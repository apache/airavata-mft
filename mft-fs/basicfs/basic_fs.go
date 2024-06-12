package basicfs

import (
	"context"
	"crypto/rand"
	"fmt"
	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseops"
	"github.com/jacobsa/fuse/fuseutil"
	"mft-fs/datastructures"
	"os"
	"syscall"
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

	file, _ := os.OpenFile("log.txt", os.O_APPEND|os.O_CREATE|os.O_WRONLY, 0644)
	defer file.Close()
	file.WriteString(fmt.Sprintf("%s\n", message))
}

func expireTime() time.Time {
	return time.Now().Add(time.Minute)
}

type BasicFS struct {
	fuseutil.NotImplementedFileSystem
	root string
	// pathToInode map[string]fuseops.InodeID
	inodeToPath   map[fuseops.InodeID]string
	handleToInode map[fuseops.HandleID]fuseops.InodeID
}

/*
todo:
	OS Operations: MkNode, SetINodeAttrs
	Dir Operations:
	File Operations: ReadSymLink, CreateSymLink, CreateLink, Unlink
	Correctness
		- atomicity checks
		- sync vs flush
		- buffer writing
	Optimizations:
		- update map when appropriate (mkdir, createfile, rmdir, etc.)
		- store tree backing
		- explore caching strategies
*/

// private helpers

func (fs BasicFS) update() error {
	q := datastructures.NewQueue()
	q.Enqueue(fs.root)
	for !q.IsEmpty() {
		path := q.Dequeue().(string)
		fileInfo, e := os.Stat(path)
		if e != nil {
			return e
		}
		inode := fileInfo.Sys().(*syscall.Stat_t).Ino
		fs.inodeToPath[fuseops.InodeID(inode)] = path
		if fileInfo.IsDir() {
			files, e := os.ReadDir(path)
			if e != nil {
				return e
			}
			for _, file := range files {
				q.Enqueue(path + "/" + file.Name())
			}
		}
	}
	return nil
}

func (fs BasicFS) fillAttributes(path string, attributes *fuseops.InodeAttributes) error {

	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	stats := fileInfo.Sys().(*syscall.Stat_t)

	attributes.Size = uint64(fileInfo.Size())

	attributes.Nlink = 1

	attributes.Mode = fileInfo.Mode()

	attributes.Atime = time.Unix(stats.Atimespec.Sec, stats.Atimespec.Nsec)
	attributes.Mtime = time.Unix(stats.Mtimespec.Sec, stats.Mtimespec.Nsec)
	attributes.Ctime = time.Unix(stats.Ctimespec.Sec, stats.Ctimespec.Nsec)
	attributes.Crtime = time.Unix(stats.Birthtimespec.Sec, stats.Birthtimespec.Nsec)

	attributes.Uid = stats.Uid
	attributes.Gid = stats.Gid

	return nil
}

func (fs BasicFS) generateHandle(inode fuseops.InodeID) (fuseops.HandleID, error) {
	var buff [8]byte
	_, e := rand.Read(buff[:])
	if e != nil {
		return 0, e
	}
	var output uint64 = 0
	for i := 0; i < len(buff); i++ {
		output = output | uint64(buff[i]<<(8*i))
	}
	handle := fuseops.HandleID(output)
	_, ok := fs.handleToInode[handle]
	if ok {
		return fs.generateHandle(inode)
	}
	fs.handleToInode[handle] = inode
	return handle, nil
}

// Public BasicFS specific methods

func NewBasicFS(root string) (BasicFS, error) {
	fs := BasicFS{
		root: root,
		// pathToInode: make(map[string]fuseops.InodeID),
		inodeToPath:   make(map[fuseops.InodeID]string),
		handleToInode: make(map[fuseops.HandleID]fuseops.InodeID),
	}
	fs.inodeToPath[1] = fs.root
	e := fs.update()
	return fs, e
}

// FUSE Operations Implementation

func (fs BasicFS) StatFS(ctx context.Context, op *fuseops.StatFSOp) error {
	printer("StatFS")

	stat, _ := os.Stat(fs.root)

	op.BlockSize = 4096

	op.Blocks = 64
	op.BlocksFree = uint64(64 - uint32(stat.Size())/op.BlockSize)
	op.BlocksAvailable = op.BlocksFree

	op.IoSize = 4096

	op.Inodes = 128
	op.InodesFree = op.Inodes - uint64(len(fs.inodeToPath))

	return nil
}

func (fs BasicFS) LookUpInode(ctx context.Context, op *fuseops.LookUpInodeOp) error {
	printer("LookUpInode")
	var path string
	var ok bool
	// find parent
	if op.Parent == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Parent]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
			path, ok = fs.inodeToPath[op.Parent]
			// if parent does not exist
			if !ok {
				return fuse.ENOENT // FileDoesNotExistError{}
			}
		}
	}

	// get parent information
	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	// if parent is not a directory
	if !fileInfo.IsDir() {
		return fuse.ENOTDIR // not a directory error
	}

	// get child information
	fileInfo, e = os.Stat(path + "/" + op.Name)

	if e != nil {
		// if child does not exist, return without updating op contents
		return nil
	}

	// if child exists, update op contents
	stat := fileInfo.Sys().(*syscall.Stat_t)

	op.Entry.EntryExpiration = expireTime()
	op.Entry.AttributesExpiration = expireTime()

	op.Entry.Child = fuseops.InodeID(stat.Ino)

	return fs.fillAttributes(path+"/"+op.Name, &op.Entry.Attributes)
}

func (fs BasicFS) GetInodeAttributes(ctx context.Context, op *fuseops.GetInodeAttributesOp) error {
	printer("GetInodeAttributes")
	var path string
	var ok bool
	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
			path, ok = fs.inodeToPath[op.Inode]
			if !ok {
				return fuse.ENOENT
			}
		}
	}

	op.AttributesExpiration = expireTime()
	return fs.fillAttributes(path, &op.Attributes)
}

func (fs BasicFS) SetInodeAttributes(ctx context.Context, op *fuseops.SetInodeAttributesOp) error {
	printer("SetInodeAttributes")

	var path string
	var ok bool

	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
			path, ok = fs.inodeToPath[op.Inode]
			if !ok {
				return fuse.ENOENT
			}
		}

	}

	var e error

	if op.Mode != nil {
		e = os.Chmod(path, *op.Mode)
		if e != nil {
			return e
		}
	}

	uid := -1
	gid := -1
	if op.Uid != nil {
		uid = int(*op.Uid)
	}
	if op.Gid != nil {
		gid = int(*op.Gid)
	}
	e = os.Chown(path, uid, gid)
	if e != nil {
		return e
	}

	atime := time.Time{}
	mtime := time.Time{}
	if op.Atime != nil {
		atime = *op.Atime
	}
	if op.Mtime != nil {
		mtime = *op.Mtime
	}
	e = os.Chtimes(path, atime, mtime)
	if e != nil {
		return e
	}

	e = fs.fillAttributes(path, &op.Attributes)
	if e != nil {
		return e
	}

	op.AttributesExpiration = expireTime()

	return nil
}

// ForgetInode
// todo account for references and hard links
func (fs BasicFS) ForgetInode(ctx context.Context, op *fuseops.ForgetInodeOp) error {
	printer("ForgetInode")
	delete(fs.inodeToPath, op.Inode)
	return nil
}

func (fs BasicFS) BatchForget(ctx context.Context, op *fuseops.BatchForgetOp) error {
	printer("BatchForget")
	for _, entry := range op.Entries {
		delete(fs.inodeToPath, entry.Inode)
	}
	return nil
}

func (fs BasicFS) MkDir(ctx context.Context, op *fuseops.MkDirOp) error {
	printer("MkDir")

	var path string
	var ok bool
	// find parent
	if op.Parent == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Parent]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
			path, ok = fs.inodeToPath[op.Parent]
			// if parent does not exist
			if !ok {
				return fuse.ENOENT
			}
		}
	}

	// get parent information
	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	// if parent is not a directory
	if !fileInfo.IsDir() {
		return fuse.ENOTDIR
	}

	childPath := path + "/" + op.Name
	e = os.Mkdir(childPath, op.Mode)
	switch e.(type) {
	case nil:
		break
	case *os.PathError:
		return fuse.EEXIST
	default:
		return e
	}

	fileInfo, e = os.Stat(childPath)
	if e != nil {
		return e
	}

	op.Entry.Child = fuseops.InodeID(fileInfo.Sys().(*syscall.Stat_t).Ino)
	op.Entry.AttributesExpiration = expireTime()
	op.Entry.EntryExpiration = expireTime()

	return fs.fillAttributes(childPath, &op.Entry.Attributes)
}

//func (fs BasicFS) MkNode(ctx context.Context, op *fuseops.MkNodeOp) error {
//	printer("MkNode")
//	return NoImplementationError{}
//}

func (fs BasicFS) CreateFile(ctx context.Context, op *fuseops.CreateFileOp) error {
	printer("CreateFile")

	var path string
	var ok bool
	// find parent
	if op.Parent == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Parent]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
			path, ok = fs.inodeToPath[op.Parent]
			// if parent does not exist
			if !ok {
				return fuse.ENOENT
			}
		}
	}

	// get parent information
	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	// if parent is not a directory
	if !fileInfo.IsDir() {
		return fuse.ENOTDIR // not a directory error
	}

	childPath := path + "/" + op.Name
	var file *os.File
	file, e = os.Create(childPath)
	defer file.Close()
	switch e.(type) {
	case nil:
		break
	case *os.PathError:
		return fuse.EEXIST
	default:
		return e
	}
	e = file.Chmod(op.Mode)
	if e != nil {
		return e
	}

	fileInfo, e = os.Stat(childPath)
	if e != nil {
		return e
	}

	op.Entry.Child = fuseops.InodeID(fileInfo.Sys().(*syscall.Stat_t).Ino)
	op.Entry.AttributesExpiration = expireTime()
	op.Entry.EntryExpiration = expireTime()

	var handle fuseops.HandleID
	handle, e = fs.generateHandle(op.Entry.Child)
	if e != nil {
		return e
	}
	op.Handle = handle

	return fs.fillAttributes(childPath, &op.Entry.Attributes)
}

//func (fs BasicFS) CreateLink(ctx context.Context, op *fuseops.CreateLinkOp) error {
//	printer("CreateLink")
//	return NoImplementationError{}
//}
//
//func (fs BasicFS) CreateSymlink(ctx context.Context, op *fuseops.CreateSymlinkOp) error {
//	printer("CreateSymlink")
//	return NoImplementationError{}
//}

// Rename
//todo: verify atomicity requirements
/*
If the new name is an existing directory, the file system must ensure it is empty before replacing it, returning ENOTEMPTY otherwise. (This is per the posix spec: http://goo.gl/4XtT79)

The rename must be atomic from the point of view of an observer of the new name. That is, if the new name already exists, there must be no point at which it doesn't exist.

It is okay for the new name to be modified before the old name is removed; these need not be atomic. In fact, the Linux man page explicitly says this is likely (cf. https://goo.gl/Y1wVZc).

Linux bends over backwards (https://goo.gl/pLDn3r) to ensure that neither the old nor the new parent can be concurrently modified. But it's not clear whether OS X does this, and in any case it doesn't matter for file systems that may be modified remotely. Therefore a careful file system implementor should probably ensure if possible that the unlink step in the "link new name, unlink old name" process doesn't unlink a different inode than the one that was linked to the new name. Still, posix and the man pages are imprecise about the actual semantics of a rename if it's not atomic, so it is probably not disastrous to be loose about this.
*/
func (fs BasicFS) Rename(ctx context.Context, op *fuseops.RenameOp) error {
	printer("Rename")

	e := fs.update()
	if e != nil {
		return e
	}

	var path string
	var newPath string
	var oldPath string
	var ok bool

	path, ok = fs.inodeToPath[op.OldParent]
	if !ok {
		return fuse.ENOENT
	}
	oldPath = path + "/" + op.OldName

	path, ok = fs.inodeToPath[op.NewParent]
	if ok {
		newPath = path + "/" + op.NewName
		fileInfo, e := os.Stat(path)
		if e == nil && fileInfo.IsDir() {
			files, e := os.ReadDir(newPath)
			if e != nil {
				return e
			}
			if len(files) > 0 {
				return fuse.ENOTEMPTY
			}
		}
	}

	return os.Rename(oldPath, newPath)
}

func (fs BasicFS) RmDir(ctx context.Context, op *fuseops.RmDirOp) error {
	printer("RmDir")

	var path string
	var ok bool

	path, ok = fs.inodeToPath[op.Parent]
	if !ok {
		e := fs.update()
		if e != nil {
			return e
		}
		path, ok = fs.inodeToPath[op.Parent]
		if !ok {
			return fuse.ENOENT
		}
	}

	path = path + "/" + op.Name
	return os.RemoveAll(path)
}

//func (fs BasicFS) Unlink(ctx context.Context, op *fuseops.UnlinkOp) error {
//	printer("Unlink")
//	return NoImplementationError{}
//}

func (fs BasicFS) OpenDir(ctx context.Context, op *fuseops.OpenDirOp) error {
	printer("OpenDir")
	var ok bool
	_, ok = fs.inodeToPath[op.Inode]
	if !ok && op.Inode != 1 {
		e := fs.update()
		if e != nil {
			return e
		}
		_, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}
	handle, e := fs.generateHandle(op.Inode)
	if e != nil {
		return e
	}
	op.Handle = handle
	return nil
}

// todo: handle offsets (see ReadDirOp documentation)
func (fs BasicFS) ReadDir(ctx context.Context, op *fuseops.ReadDirOp) error {
	printer("ReadDir")

	var path string
	var ok bool
	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
			path, ok = fs.inodeToPath[op.Inode]
			if !ok {
				return fuse.ENOENT
			}
		}
	}
	var inode fuseops.InodeID
	inode, ok = fs.handleToInode[op.Handle]
	if !ok || inode != op.Inode {
		return fuse.ENOENT
	}

	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	if !fileInfo.IsDir() {
		return fuse.ENOTDIR
	}

	data, e := os.ReadDir(path)
	if e != nil {
		return e
	}

	printer(fmt.Sprintf("Path: %v\tOffset: %v\n", path, op.Offset))

	//if fuseops.DirOffset(len(data)) < op.Offset {
	//	return nil
	//}

	op.BytesRead = 0
	bytesRead := 0
	currentBytesRead := 0
	buff := make([]byte, 1024)
	for _, d := range data {
		dirent := fuseutil.Dirent{}
		fileInfo, e := d.Info()
		if e != nil {
			return e
		}
		dirent.Inode = fuseops.InodeID(fileInfo.Sys().(*syscall.Stat_t).Ino)
		dirent.Name = d.Name()
		if fileInfo.IsDir() {
			dirent.Type = fuseutil.DT_Directory
		} else {
			dirent.Type = fuseutil.DT_File
		}
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
		Inode:  inode,
	})
	bytesRead += currentBytesRead
	op.BytesRead += currentBytesRead

	// op.Dst = op.Dst[op.Offset:]
	// op.BytesRead = bytesRead - int(op.Offset)

	printer(fmt.Sprintf("Bytes Read: %v\n", op.BytesRead))

	fmt.Println("done")

	return nil
}

func (fs BasicFS) ReleaseDirHandle(ctx context.Context, op *fuseops.ReleaseDirHandleOp) error {
	printer("ReleaseDirHandle")

	delete(fs.handleToInode, op.Handle)

	return nil
}

func (fs BasicFS) OpenFile(ctx context.Context, op *fuseops.OpenFileOp) error {
	printer("OpenFile")
	var ok bool
	_, ok = fs.inodeToPath[op.Inode]
	if !ok && op.Inode != 1 {
		e := fs.update()
		if e != nil {
			return e
		}
		_, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}
	handle, e := fs.generateHandle(op.Inode)
	if e != nil {
		return e
	}
	op.Handle = handle
	return nil
}

func (fs BasicFS) ReadFile(ctx context.Context, op *fuseops.ReadFileOp) error {
	printer("ReadFile")

	var path string
	var ok bool
	path, ok = fs.inodeToPath[op.Inode]
	if !ok {
		e := fs.update()
		if e != nil {
			return e
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}
	var inode fuseops.InodeID
	inode, ok = fs.handleToInode[op.Handle]
	if !ok || inode != op.Inode {
		return fuse.ENOENT
	}

	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	var file *os.File
	file, e = os.OpenFile(path, os.O_RDWR, fileInfo.Mode())
	defer file.Close()
	if e != nil {
		return e
	}

	if op.Dst != nil {
		buff := make([]byte, op.Size)
		byteCount, e := file.ReadAt(buff, op.Offset)
		if e != nil {
			return e
		}
		op.BytesRead = int(min(int64(byteCount), op.Size))
		for i := 0; i < op.BytesRead; i++ {
			op.Dst[i] = buff[i] //append(op.Dst, buff[i])
		}
		printer(string(buff))
		printer(fmt.Sprintf("read requested at offset: %v\tbytes read: %v", op.Offset, byteCount))
		return nil
	}

	fmt.Println("vector read requested")

	return NoImplementationError{}
}

func (fs BasicFS) WriteFile(ctx context.Context, op *fuseops.WriteFileOp) error {
	printer("WriteFile")

	var path string
	var ok bool
	path, ok = fs.inodeToPath[op.Inode]
	if !ok {
		e := fs.update()
		if e != nil {
			return e
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}
	var inode fuseops.InodeID
	inode, ok = fs.handleToInode[op.Handle]
	if !ok || inode != op.Inode {
		return fuse.ENOENT
	}

	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	var file *os.File
	file, e = os.OpenFile(path, os.O_RDWR, fileInfo.Mode())
	defer file.Close()
	if e != nil {
		return e
	}

	_, e = file.WriteAt(op.Data, op.Offset)
	if e != nil {
		return e
	}

	return nil
}

func (fs BasicFS) SyncFile(ctx context.Context, op *fuseops.SyncFileOp) error {
	printer("SyncFile")

	var path string
	var ok bool

	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}

	if fs.handleToInode[op.Handle] != op.Inode {
		return fuse.ENOENT
	}

	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	file, e := os.OpenFile(path, os.O_RDWR, fileInfo.Mode())
	defer file.Close()
	if e != nil {
		return e
	}

	e = file.Sync()
	if e != nil {
		return e
	}

	return nil
}

func (fs BasicFS) FlushFile(ctx context.Context, op *fuseops.FlushFileOp) error {
	printer("FlushFile")

	var path string
	var ok bool

	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}

	if fs.handleToInode[op.Handle] != op.Inode {
		return fuse.ENOENT
	}

	fileInfo, e := os.Stat(path)
	if e != nil {
		return e
	}
	file, e := os.OpenFile(path, os.O_RDWR, fileInfo.Mode())
	defer file.Close()
	if e != nil {
		return e
	}

	e = file.Sync()
	if e != nil {
		return e
	}

	return nil
}

func (fs BasicFS) ReleaseFileHandle(ctx context.Context, op *fuseops.ReleaseFileHandleOp) error {
	printer("ReleaseFileHandle")

	delete(fs.handleToInode, op.Handle)

	return nil
}

//func (fs BasicFS) ReadSymlink(ctx context.Context, op *fuseops.ReadSymlinkOp) error {
//	printer("ReadSymLink")
//	return NoImplementationError{}
//}

/*
func (fs BasicFS) RemoveXattr(ctx context.Context, op *fuseops.RemoveXattrOp) error {
	printer("RemoveXattr")

	var path string
	var ok bool

	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}

	e := xattr.Remove(path, op.Name)
	if e != nil {
		return fuse.ENOATTR
	}

	return nil
}

func (fs BasicFS) GetXattr(ctx context.Context, op *fuseops.GetXattrOp) error {
	printer("GetXattr")

	var path string
	var ok bool

	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}

	val, e := xattr.Get(path, op.Name)
	if e != nil {
		return fuse.ENOATTR
	}

	op.Dst = val
	op.BytesRead = len(val)

	return nil
}

func (fs BasicFS) ListXattr(ctx context.Context, op *fuseops.ListXattrOp) error {
	printer("ListXattr")

	var path string
	var ok bool

	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}

	val, e := xattr.List(path)
	if e != nil {
		return e
	}

	var output []byte
	for _, v := range val {
		output = append(output, []byte(v)...)
		output = append(output, 0)
	}

	op.Dst = output
	op.BytesRead = len(output)

	return nil
}

func (fs BasicFS) SetXattr(ctx context.Context, op *fuseops.SetXattrOp) error {
	printer("SetXattr")

	var path string
	var ok bool

	if op.Inode == 1 {
		path = fs.root
	} else {
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			e := fs.update()
			if e != nil {
				return e
			}
		}
		path, ok = fs.inodeToPath[op.Inode]
		if !ok {
			return fuse.ENOENT
		}
	}

	_, e := xattr.Get(path, op.Name)

	if op.Flags == 0x1 && e == nil {
		return fuse.EEXIST
	} else if op.Flags == 0x2 && e != nil {
		return fuse.ENOATTR
	}

	e = xattr.Set(path, op.Name, op.Value)
	if e != nil {
		return e
	}

	return nil
}

func (fs BasicFS) Fallocate(ctx context.Context, op *fuseops.FallocateOp) error {
	printer("Fallocate")
	return NoImplementationError{}
}
*/

func (fs BasicFS) Destroy() {
	printer("Destroy")
	fuse.Unmount("./mount")
}
