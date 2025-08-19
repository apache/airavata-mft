/*
Package abstractfs
abstractfs is a FUSE-based filesystem wrapper
it defines an interface for a filesystem manager, which can be implemented by any filesystem
it implements FUSE operations which calls the respective methods of the filesystems
*/
package abstractfs

import (
	"context"
	"fmt"
	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseops"
	"github.com/jacobsa/fuse/fuseutil"
	"time"
)

type NoImplementationError struct{}

func (e NoImplementationError) Error() string {
	return "No implementation"
}

/*
The printer function prints messages to the terminal on behalf of FUSE operations.
*/
func printer(message string) {
	fmt.Println(message)

	/*file, _ := os.OpenFile("log.txt", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	defer file.Close()
	file.WriteString(fmt.Sprintf("%s\n", message))*/
}

func minimum(a int64, b int64) int64 {
	if a < b {
		return a
	}
	return b
}

func expireTime() time.Time {
	return time.Now().Add(time.Minute)
}

type AbstractFS struct {
	fuseutil.NotImplementedFileSystem
	Manager FSManager
}

// private helpers

func (fs AbstractFS) fillAttributes(inode fuseops.InodeID, attributes *fuseops.InodeAttributes) error {
	info, e := fs.Manager.GetInfo(inode)
	if e != nil {
		return fuse.ENOENT
	}

	attributes.Size = info.Size

	attributes.Nlink = info.Nlink

	attributes.Mode = info.Mode

	attributes.Atime = info.Atime
	attributes.Mtime = info.Mtime
	attributes.Ctime = info.Ctime
	attributes.Crtime = info.Crtime

	attributes.Uid = info.Uid
	attributes.Gid = info.Gid

	return nil
}

// public methods

func NewAbstractFS(manager FSManager) (AbstractFS, error) {
	fs := AbstractFS{
		Manager: manager,
	}
	return fs, nil
}

// FUSE Operations

func (fs AbstractFS) StatFS(ctx context.Context, op *fuseops.StatFSOp) error {
	printer("StatFS")

	op.BlockSize = 4096

	size, e := fs.Manager.GetSize()
	op.Blocks = 64
	op.BlocksFree = uint64(64 - uint32(size)/op.BlockSize)
	op.BlocksAvailable = op.BlocksFree

	op.IoSize = 4096

	op.Inodes = 128
	length, e := fs.Manager.GetLength()
	if e != nil {
		return e
	}
	op.InodesFree = op.Inodes - length

	return nil
}

func (fs AbstractFS) LookUpInode(ctx context.Context, op *fuseops.LookUpInodeOp) error {
	printer("LookUpInode")

	// does parent exist?
	parentInfo, e := fs.Manager.GetInfo(op.Parent)
	if e != nil {
		return fuse.ENOENT
	}

	// is parent a directory?
	if parentInfo.DirentType != fuseutil.DT_Directory {
		return fuse.ENOTDIR
	}

	// does child exist in parent?
	index, ok := parentInfo.ChildrenIndexMap[op.Name]
	if !ok {
		return nil // fuse.ENOENT
	}

	// get child's inode
	childInode := parentInfo.Children[index]

	// fill information
	op.Entry.Child = childInode
	op.Entry.AttributesExpiration = expireTime()
	op.Entry.EntryExpiration = expireTime()

	return fs.fillAttributes(childInode, &op.Entry.Attributes)
}

func (fs AbstractFS) GetInodeAttributes(ctx context.Context, op *fuseops.GetInodeAttributesOp) error {
	printer("GetInodeAttributes")

	op.AttributesExpiration = expireTime()
	return fs.fillAttributes(op.Inode, &op.Attributes)
}

func (fs AbstractFS) SetInodeAttributes(ctx context.Context, op *fuseops.SetInodeAttributesOp) error {
	printer("SetInodeAttributes")
	op.AttributesExpiration = expireTime()
	e := fs.Manager.SetInfo(op.Inode, op.Uid, op.Gid, op.Size, op.Mode, op.Atime, op.Mtime)
	if e != nil {
		return e
	}
	return fs.fillAttributes(op.Inode, &op.Attributes)
}

// ForgetInode
// todo account for references and hard links
func (fs AbstractFS) ForgetInode(ctx context.Context, op *fuseops.ForgetInodeOp) error {
	printer("ForgetInode")
	return fs.Manager.Delete(op.Inode)
}

func (fs AbstractFS) BatchForget(ctx context.Context, op *fuseops.BatchForgetOp) error {
	printer("BatchForget")
	var e error
	for _, entry := range op.Entries {
		e = fs.Manager.Delete(entry.Inode)
		if e != nil {
			return e
		}
	}
	return nil
}

func (fs AbstractFS) MkDir(ctx context.Context, op *fuseops.MkDirOp) error {
	printer("MkDir")

	// get parent information
	info, e := fs.Manager.GetInfo(op.Parent)
	if e != nil {
		return e
	}

	// check if parent is a directory
	if info.DirentType != fuseutil.DT_Directory {
		return fuse.ENOTDIR
	}

	// already exists?
	_, ok := info.ChildrenIndexMap[op.Name]
	if ok {
		return fuse.EEXIST
	}

	childInode, e := fs.Manager.MkDir(op.Parent, op.Name, op.Mode)
	if e != nil {
		return e
	}

	op.Entry.AttributesExpiration = expireTime()
	op.Entry.EntryExpiration = expireTime()
	op.Entry.Child = childInode
	return fs.fillAttributes(childInode, &op.Entry.Attributes)
}

func (fs AbstractFS) CreateFile(ctx context.Context, op *fuseops.CreateFileOp) error {
	printer("CreateFile")

	// get parent information
	info, e := fs.Manager.GetInfo(op.Parent)
	if e != nil {
		return e
	}

	// check if parent is a directory
	if info.DirentType != fuseutil.DT_Directory {
		return fuse.ENOTDIR
	}

	// already exists?
	_, ok := info.ChildrenIndexMap[op.Name]
	if ok {
		return fuse.EEXIST
	}

	childInode, e := fs.Manager.CreateFile(op.Parent, op.Name, op.Mode)
	if e != nil {
		return e
	}

	op.Entry.AttributesExpiration = expireTime()
	op.Entry.EntryExpiration = expireTime()
	op.Entry.Child = childInode
	op.Handle, e = fs.Manager.GenerateHandle(childInode)
	if e != nil {
		return e
	}
	return fs.fillAttributes(childInode, &op.Entry.Attributes)
}

func (fs AbstractFS) RmDir(ctx context.Context, op *fuseops.RmDirOp) error {
	printer("RmDir")

	info, e := fs.Manager.GetInfo(op.Parent)
	if e != nil {
		return e
	}

	if info.DirentType != fuseutil.DT_Directory {
		return fuse.ENOTDIR
	}

	inode := info.Children[info.ChildrenIndexMap[op.Name]]
	return fs.Manager.RmDir(inode)
}

func (fs AbstractFS) OpenDir(ctx context.Context, op *fuseops.OpenDirOp) error {
	printer("OpenDir")
	handle, e := fs.Manager.GenerateHandle(op.Inode)
	if e != nil {
		return e
	}
	op.Handle = handle
	return nil
}

func (fs AbstractFS) ReadDir(ctx context.Context, op *fuseops.ReadDirOp) error {
	printer("ReadDir")

	info, e := fs.Manager.GetInfo(op.Inode)
	if e != nil {
		return e
	}
	if info.DirentType != fuseutil.DT_Directory {
		return fuse.ENOTDIR
	}
	if info.Handle != op.Handle {
		return fuse.ENOENT
	}

	printer(fmt.Sprintf("Path: %v\tOffset: %v\n", info.Path, op.Offset))

	//if fuseops.DirOffset(len(data)) < op.Offset {
	//	return nil
	//}

	op.BytesRead = 0
	bytesRead := 0
	currentBytesRead := 0
	buff := make([]byte, 1024)
	for _, childInode := range info.Children {
		dirent := fuseutil.Dirent{}
		childInfo, e := fs.Manager.GetInfo(childInode)
		if e != nil {
			return e
		}
		dirent.Inode = childInfo.Inode
		dirent.Name = childInfo.Name
		dirent.Type = childInfo.DirentType
		dirent.Offset = fuseops.DirOffset(bytesRead)          // - op.Offset
		currentBytesRead = fuseutil.WriteDirent(buff, dirent) //op.Dst[bytesRead:], dirent)
		if bytesRead >= int(op.Offset) {
			copy(op.Dst[op.BytesRead:], buff)
			op.BytesRead += currentBytesRead
			printer(fmt.Sprintf("Inode: %v\tName: %v\tOffset: %v\n", dirent.Inode, dirent.Name, dirent.Offset))
		}
		bytesRead += currentBytesRead
	}

	if int(op.Offset) >= bytesRead {
		return nil
	}

	currentBytesRead = fuseutil.WriteDirent(op.Dst[op.BytesRead:], fuseutil.Dirent{
		Offset: fuseops.DirOffset(bytesRead),
		Name:   ".",
		Type:   fuseutil.DT_Directory,
		Inode:  op.Inode,
	})
	bytesRead += currentBytesRead
	op.BytesRead += currentBytesRead

	// op.Dst = op.Dst[op.Offset:]
	// op.BytesRead = bytesRead - int(op.Offset)

	printer(fmt.Sprintf("Bytes Read: %v\n", op.BytesRead))

	fmt.Println("done")

	return nil
}

func (fs AbstractFS) ReleaseDirHandle(ctx context.Context, op *fuseops.ReleaseDirHandleOp) error {
	printer("ReleaseDirHandle")

	return fs.Manager.DeleteHandle(op.Handle)
}

func (fs AbstractFS) OpenFile(ctx context.Context, op *fuseops.OpenFileOp) error {
	printer("OpenFile")
	handle, e := fs.Manager.GenerateHandle(op.Inode)
	if e != nil {
		return e
	}
	op.Handle = handle
	return nil
}

func (fs AbstractFS) ReadFile(ctx context.Context, op *fuseops.ReadFileOp) error {
	printer("ReadFile")

	info, e := fs.Manager.GetInfo(op.Inode)
	if e != nil {
		return e
	}

	if op.Handle != info.Handle {
		return fuse.ENOENT
	}

	if op.Dst != nil {
		buff := make([]byte, op.Size)
		byteCount, _ := fs.Manager.ReadAt(op.Inode, buff, op.Offset)
		/*if e != nil {
			return e
		}*/
		op.BytesRead = int(minimum(int64(byteCount), op.Size))
		for i := 0; i < op.BytesRead; i++ {
			op.Dst[i] = buff[i] //append(op.Dst, buff[i])
		}
		printer(string(buff))
		printer(fmt.Sprintf("read requested at offset: %v\tbytes read: %v", op.Offset, byteCount))
		return nil
	}

	// todo implement vector read
	fmt.Println("vector read requested")

	return NoImplementationError{}
}

func (fs AbstractFS) WriteFile(ctx context.Context, op *fuseops.WriteFileOp) error {
	printer("WriteFile")

	info, e := fs.Manager.GetInfo(op.Inode)
	if e != nil {
		return e
	}
	if info.Handle != op.Handle {
		return fuse.ENOENT
	}

	_, e = fs.Manager.WriteAt(op.Inode, op.Data, op.Offset)
	if e != nil {
		return e
	}

	return nil
}

func (fs AbstractFS) SyncFile(ctx context.Context, op *fuseops.SyncFileOp) error {
	printer("SyncFile")

	info, e := fs.Manager.GetInfo(op.Inode)
	if e != nil {
		return e
	}
	if info.Handle != op.Handle {
		return fuse.ENOENT
	}

	return fs.Manager.SyncFile(op.Inode)
}

func (fs AbstractFS) FlushFile(ctx context.Context, op *fuseops.FlushFileOp) error {
	printer("SyncFile")

	info, e := fs.Manager.GetInfo(op.Inode)
	if e != nil {
		return e
	}
	if info.Handle != op.Handle {
		return fuse.ENOENT
	}

	return fs.Manager.SyncFile(op.Inode)
}

func (fs AbstractFS) ReleaseFileHandle(ctx context.Context, op *fuseops.ReleaseFileHandleOp) error {
	printer("ReleaseFileHandle")

	return fs.Manager.DeleteHandle(op.Handle)
}

func (fs AbstractFS) Destroy() {
	printer("Destroy")
	fs.Manager.Destroy()
	fuse.Unmount("./mount")
}
