package server

import (
	"fmt"
	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseops"
	"github.com/jacobsa/fuse/fuseutil"
	"golang.org/x/sys/unix"
	"mft-fs/archive/abstractfs"
	"mft-fs/datastructures"
	"os"
	"syscall"
	"time"
)

type SafeOSFSManager struct {
	abstractfs.FSManager
	root         string
	lock         *datastructures.CREWResource
	inodeInfo    map[fuseops.InodeID]*abstractfs.FileInfo
	inodeCounter fuseops.InodeID
}

type parentChildPair struct {
	parent fuseops.InodeID
	child  string
}

func (manager *SafeOSFSManager) bfs(root string) {
	q := datastructures.NewQueue()
	q.Enqueue(&(parentChildPair{
		parent: 0,
		child:  root,
	}))
	for !q.IsEmpty() {
		current := q.Dequeue().(*parentChildPair)
		var direntType fuseutil.DirentType
		stat, e := os.Stat(current.child)
		if e != nil {
			fmt.Println("error:")
			fmt.Println(e.Error())
			continue
		}
		if stat.IsDir() {
			dir, e := os.ReadDir(current.child)
			if e != nil {
				continue
			}
			for _, file := range dir {
				q.Enqueue(&(parentChildPair{
					parent: manager.inodeCounter,
					child:  fmt.Sprintf("%s/%s", current.child, file.Name()),
				}))
			}
			direntType = fuseutil.DT_Directory
		} else {
			direntType = fuseutil.DT_File
		}
		info := abstractfs.NewSafeFileInfo(stat.Name(), current.child, manager.inodeCounter, current.parent, direntType)
		manager.inodeInfo[manager.inodeCounter] = &info
		parentInfo, ok := manager.inodeInfo[current.parent]
		if ok {
			parentInfo.ChildrenIndexMap[info.Name] = len(parentInfo.Children)
			parentInfo.Children = append(parentInfo.Children, manager.inodeCounter)
		}
		manager.updateInfo(manager.inodeInfo[manager.inodeCounter])
		manager.inodeCounter++
	}
}

func NewSafeOSFSManager(root string) *SafeOSFSManager {
	manager := &SafeOSFSManager{
		root:         root,
		lock:         datastructures.NewCREWResource(),
		inodeInfo:    make(map[fuseops.InodeID]*abstractfs.FileInfo),
		inodeCounter: 1,
	}
	manager.lock.RequestWrite()
	defer manager.lock.AckWrite()
	manager.bfs(root)
	return manager
}

func (manager *SafeOSFSManager) GenerateHandle(inode fuseops.InodeID) (fuseops.HandleID, error) {
	return 0, nil
}

func (manager *SafeOSFSManager) updateInfo(info *abstractfs.FileInfo) error {

	fileInfo, e := os.Stat(info.Path)
	if e != nil {
		return e
	}
	stats := fileInfo.Sys().(*syscall.Stat_t)

	info.Size = uint64(fileInfo.Size())

	info.Nlink = 1

	info.Mode = fileInfo.Mode()

	/*info.Atime = time.Unix(stats.Atimespec.Sec, stats.Atimespec.Nsec)
	info.Mtime = time.Unix(stats.Mtimespec.Sec, stats.Mtimespec.Nsec)
	info.Ctime = time.Unix(stats.Ctimespec.Sec, stats.Ctimespec.Nsec)
	info.Crtime = time.Unix(stats.Birthtimespec.Sec, stats.Birthtimespec.Nsec)*/
	var timeStats unix.Stat_t
	e = unix.Stat(info.Path, &timeStats)
	if e != nil {
		return e
	}
	info.Atime = time.Unix(timeStats.Atim.Sec, timeStats.Atim.Nsec)
	info.Mtime = time.Unix(timeStats.Mtim.Sec, timeStats.Mtim.Nsec)
	info.Ctime = time.Unix(timeStats.Ctim.Sec, timeStats.Ctim.Nsec)
	info.Crtime = time.Unix(timeStats.Ctim.Sec, timeStats.Ctim.Nsec)

	info.Uid = stats.Uid
	info.Gid = stats.Gid

	info.MetadataWriteTime = time.Now()

	return nil
}

// todo: consider locking mechanisms
func (manager *SafeOSFSManager) GetSize() (uint64, error) {
	stat, _ := os.Stat(manager.root)
	return uint64(stat.Size()), nil
}

func (manager *SafeOSFSManager) GetLength() (uint64, error) {
	manager.lock.RequestRead()
	defer manager.lock.AckRead()
	output := uint64(len(manager.inodeInfo))
	return output, nil
}

func (manager *SafeOSFSManager) GetInfo(inode fuseops.InodeID) (*abstractfs.FileInfo, error) {
	manager.lock.RequestRead()
	defer manager.lock.AckRead()
	info, ok := manager.inodeInfo[inode]
	if !ok {
		return nil, fuse.ENOENT
	}
	return info, nil
}

func (manager *SafeOSFSManager) SetInfo(inode fuseops.InodeID, uidptr *uint32, gidptr *uint32, sizeptr *uint64, modeptr *os.FileMode, atimeptr *time.Time, mtimeptr *time.Time) error {
	info, e := manager.GetInfo(inode)

	// todo consider thread safety options

	if e != nil {
		return e
	}

	if modeptr != nil {
		e = os.Chmod(info.Path, *modeptr)
		if e != nil {
			return e
		}
	}

	uid := -1
	gid := -1
	if uidptr != nil {
		uid = int(*uidptr)
	}
	if gidptr != nil {
		gid = int(*gidptr)
	}
	e = os.Chown(info.Path, uid, gid)
	if e != nil {
		return e
	}

	atime := time.Time{}
	mtime := time.Time{}
	if atimeptr != nil {
		atime = *atimeptr
	}
	if mtimeptr != nil {
		mtime = *mtimeptr
	}
	e = os.Chtimes(info.Path, atime, mtime)
	if e != nil {
		return e
	}

	info.MetadataLock.RequestWrite()
	e = manager.updateInfo(info)
	info.MetadataLock.AckWrite()

	return e
}

func (manager *SafeOSFSManager) MkDir(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	parentInfo, e := manager.GetInfo(parent)
	if e != nil {
		return 0, e
	}

	path := parentInfo.Path + "/" + name
	e = os.Mkdir(path, mode)
	if e != nil {
		return 0, e
	}

	manager.lock.RequestWrite()
	inode := manager.inodeCounter //fuseops.InodeID(osInfo.Sys().(*syscall.Stat_t).Ino)
	manager.inodeCounter++
	infoObj := abstractfs.NewSafeFileInfo(name, path, inode, parent, fuseutil.DT_Directory)
	manager.inodeInfo[inode] = &infoObj
	manager.lock.AckWrite()

	infoObj.MetadataLock.RequestWrite()
	e = manager.updateInfo(&infoObj)
	infoObj.MetadataLock.AckWrite()
	if e != nil {
		return 0, e
	}

	parentInfo.MetadataLock.RequestWrite()
	parentInfo.Children = append(parentInfo.Children, inode)
	parentInfo.ChildrenIndexMap[name] = len(parentInfo.Children) - 1
	parentInfo.MetadataWriteTime = time.Now()
	parentInfo.MetadataLock.AckWrite()

	return inode, nil
}

func (manager *SafeOSFSManager) CreateFile(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	parentInfo, e := manager.GetInfo(parent)
	if e != nil {
		return 0, e
	}

	path := parentInfo.Path + "/" + name
	file, e := os.Create(path)
	defer file.Close()
	if e != nil {
		return 0, e
	}

	e = file.Chmod(mode)
	if e != nil {
		return 0, e
	}

	manager.lock.RequestWrite()
	inode := manager.inodeCounter //fuseops.InodeID(osInfo.Sys().(*syscall.Stat_t).Ino)
	manager.inodeCounter++
	infoObj := abstractfs.NewSafeFileInfo(name, path, inode, parent, fuseutil.DT_File)
	manager.inodeInfo[inode] = &infoObj
	manager.lock.AckWrite()

	infoObj.MetadataLock.RequestWrite()
	e = manager.updateInfo(&infoObj)
	infoObj.MetadataLock.AckWrite()
	if e != nil {
		return 0, e
	}

	parentInfo.MetadataLock.RequestWrite()
	parentInfo.Children = append(parentInfo.Children, inode)
	parentInfo.ChildrenIndexMap[name] = len(parentInfo.Children) - 1
	parentInfo.MetadataWriteTime = time.Now()
	parentInfo.MetadataLock.AckWrite()

	return inode, nil
}

func (manager *SafeOSFSManager) RmDir(inode fuseops.InodeID) error {
	return nil
}

func (manager *SafeOSFSManager) Delete(inode fuseops.InodeID) error {

	// first delete any reference in parents
	// then delete self

	info, e := manager.GetInfo(inode)
	if e != nil {
		return e
	}

	parentInfo, e := manager.GetInfo(info.Parent)
	if e != nil {
		return e
	}

	parentInfo.MetadataLock.RequestWrite()
	parentInfo.Children = datastructures.Remove(parentInfo.Children, parentInfo.ChildrenIndexMap[info.Name])
	target := parentInfo.ChildrenIndexMap[info.Name]
	for name, index := range parentInfo.ChildrenIndexMap {
		if index > target {
			parentInfo.ChildrenIndexMap[name] = index - 1
		}
	}
	delete(parentInfo.ChildrenIndexMap, info.Name)
	parentInfo.MetadataWriteTime = time.Now()
	parentInfo.MetadataLock.AckWrite()

	manager.lock.RequestWrite()
	delete(manager.inodeInfo, info.Inode)
	manager.lock.AckWrite()

	return nil
}

func (manager *SafeOSFSManager) DeleteHandle(handle fuseops.HandleID) error {
	return nil
}

func (manager *SafeOSFSManager) SyncFile(inode fuseops.InodeID) error {
	info, e := manager.GetInfo(inode)
	if e != nil {
		return e
	}
	file, e := os.OpenFile(info.Path, os.O_RDWR, info.Mode)
	if e != nil {
		return e
	}
	defer file.Close()
	e = file.Sync()
	if e != nil {
		return e
	}
	return nil
}

func (manager *SafeOSFSManager) ReadAt(inode fuseops.InodeID, data []byte, off int64) (int, error) {
	info, e := manager.GetInfo(inode)
	if e != nil {
		return 0, e
	}

	file, e := os.OpenFile(info.Path, os.O_RDWR, info.Mode)
	if e != nil {
		return 0, e
	}
	defer file.Close()

	n, e := file.ReadAt(data, off)
	return n, e
}

func (manager *SafeOSFSManager) WriteAt(inode fuseops.InodeID, data []byte, off int64) (int, error) {
	info, e := manager.GetInfo(inode)
	if e != nil {
		return 0, e
	}

	file, e := os.OpenFile(info.Path, os.O_RDWR, info.Mode)
	if e != nil {
		return 0, e
	}
	defer file.Close()

	n, e := file.WriteAt(data, off)
	return n, e
}

func (manager *SafeOSFSManager) RequestRead(inode fuseops.InodeID, cache bool, cacheTime time.Time) (bool, error) {
	info, e := manager.GetInfo(inode)
	if e != nil {
		return false, e
	}

	info.ContentLock.RequestRead()
	// if cache is invalid, return lock success = true
	if !cache || info.ContentWriteTime.After(cacheTime) {
		return true, nil
	}
	info.ContentLock.AckRead()

	// if cache is valid, no need to lock
	return false, nil
}

func (manager *SafeOSFSManager) AckRead(inode fuseops.InodeID) (time.Time, error) {
	info, e := manager.GetInfo(inode)
	if e != nil {
		return time.Time{}, e
	}
	info.ContentLock.AckRead()
	info.MetadataLock.RequestRead()
	output := info.ContentWriteTime
	info.MetadataLock.AckRead()
	return output, nil
}

func (manager *SafeOSFSManager) RequestWrite(inode fuseops.InodeID, cache bool, cacheTime time.Time) (bool, error) {
	info, e := manager.GetInfo(inode)
	if e != nil {
		return false, e
	}

	info.ContentLock.RequestWrite()

	// cache is valid so grant permission
	fmt.Printf("%v\t%v\n", info.ContentWriteTime, cacheTime)
	if cache && (info.ContentWriteTime.Equal(cacheTime) || info.ContentWriteTime.Before(cacheTime)) {
		return true, nil
	}
	info.ContentLock.AckWrite()

	// cache is not valid, so do not grant permission
	return false, nil
}

func (manager *SafeOSFSManager) AckWrite(inode fuseops.InodeID) (time.Time, error) {
	info, e := manager.GetInfo(inode)
	if e != nil {
		return time.Time{}, e
	}
	info.MetadataLock.RequestWrite()
	info.ContentWriteTime = time.Now()
	output := info.ContentWriteTime
	info.MetadataLock.AckWrite()
	info.ContentLock.AckWrite()
	return output, nil
}

func (manager *SafeOSFSManager) Destroy() error { return nil }
