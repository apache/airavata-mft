package abstractfs

import (
	"os"
	"time"

	"github.com/jacobsa/fuse/fuseops"
	"github.com/jacobsa/fuse/fuseutil"

	"mft-fs/datastructures"
)

/*
FSManager Interface
the FSManager interface can be implemented by any custom filesystem
its methods are used by the AbstractFS fuse server
*/
type FSManager interface {
	/*
		Start method to initiate any FS Manager.
		params: None
		returns:
			error: any arror caused by the invocation
	*/
	Start() error

	/*
		Teardown method to close any FS Manager.
		params: None
		returns:
			error: any error caused by the invocation
	*/
	Teardown() error

	/*
		GetSize method to get the size of the filesystem
		params: None
		returns:
				uint64: the size of the filesystem, in bytes
				error: any error caused by the invocation
	*/
	GetSize() (uint64, error)

	/*
		GetLength method for getting the length of the filesystem
		params: None
		returns:
				uint64: the length of the filesystem (count the number of unique inodes)
				error: any error caused by the invocation
	*/
	GetLength() (uint64, error)

	/*
		GetInfo method for getting stats and metrics of a specific inode
		params:
				id fuseops.InodeID: the inode for which the information is required
		returns:
				*FileInfo: a reference to the FileInfo object which describes the inode's information
				error: any error caused by the invocation
	*/
	GetInfo(id fuseops.InodeID) (*FileInfo, error)

	/*
		SetInfo method for setting or modifying an inode's attributes
		params:
				id fuseops.InodeID: the inode in question
				uidptr *uint32: a pointer to the new UserID for the inode's ownership; if no change is desired, pass nil
				gidptr *uint32: a pointer to the new GroupID for the inode's ownership; if no change is desired, pass nil
				sizeptr *uint64: a pointer to the new size of the inode (in bytes); if no change is desired, pass nil
				modeptr *os.FileMode: a pointer to the new filemode of the inode; if no change is desired, pass nil
				atimeptr *time.Time: a pointer to the new access time of the inode; if no change is desired, pass nil
				mtimeptr *time.Time: a pointer to the new modification time of the inode; if no change is desired, pass nil
		returns:
				error: any error caused by the invocation
	*/
	SetInfo(id fuseops.InodeID, uidptr *uint32, gidptr *uint32, sizeptr *uint64, modeptr *os.FileMode, atimeptr *time.Time, mtimeptr *time.Time) error

	/*
		Delete method for deleting an inode from the filesystem entirely
		params:
			inode fuseops.InodeID: the inode to be deleted
		returns:
			error: any error caused by the invocation
	*/
	Delete(inode fuseops.InodeID) error

	/*
		MkDir method for creating a directory
		params:
			parent fuseops.InodeID: inode of the parent directory
			name string: name of the directory to be created
			mode os.FileMode: mode to set permissions for the new directory
		returns:
			fuseops.InodeID: the inode of the new directory
			error: any error caused by the invocation
	*/
	MkDir(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error)

	/*
		CreateFile method to create a new file
		params:
			parent fuseops.InodeID: inode of the parent directory
			name string: name of the file to be created
			mode os.FileMode: mode to set permissions for the new file
		returns:
			fuseops.InodeID: the inode of the newly created file
			error: any error caused by the invocation
	*/
	CreateFile(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error)

	/*
		RmDir method for removing a directory
		params:
			inode fuseops.InodeID: inode of the directory being deleted
		returns:
			error: any error caused by the invocation
	*/
	RmDir(inode fuseops.InodeID) error

	/*
		SyncFile method for syncing a file
		params:
			inode fuseops.InodeID: inode of the file being synced
		returns:
			error: any error caused by the invocation
	*/
	SyncFile(inode fuseops.InodeID) error

	/*
		WriteAt method for writing data to a file at a specific offset
		params:
			inode fuseops.InodeID: the inode of the file being written to
			data []byte: the bytes being written in
			off int64: the offset of the location to write to
		returns:
			n int: the number of bytes written
			err error: any error caused by the invocation
	*/
	WriteAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error)

	/*
		ReadAt method for reading from a file at a specific offset
		params:
			inode fuseops.InodeID: the inode of the file being read from
			data []byte: the buffer to store all data being read into
			off int64: the offset of the location to read from
		returns:
			n int: the number of bytes read
			err error: any error caused by the invocation
	*/
	ReadAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error)

	/*
		Destroy method for destroying a filesystem
		params: None
		returns:
			error: any error caused by the invocation
	*/
	// Destroy() error
}

/*
FileInfo struct for representing a file's information
*/
type FileInfo struct {
	// name of the file/directory
	Name string

	// path to the file/directory
	Path string

	// inode of the file/directory
	Inode fuseops.InodeID

	// if it is a directory, a slice with inodes of all children
	Children []fuseops.InodeID

	// if it is a directory, a map: name of child -> index of child in Children slice
	ChildrenIndexMap map[string]int

	// inode of the parent directory
	Parent fuseops.InodeID

	// if it is a file, the number of links
	Nlink uint32

	// size of the file/directory
	Size uint64

	// the mode of the file/directory
	Mode os.FileMode

	// access time of the file/directory
	Atime time.Time

	// modification time of the file/directory
	Mtime time.Time

	// creation time of the file/directory
	Ctime time.Time

	// creation time of the file/directory
	Crtime time.Time

	// user id of the file/directory ownership
	Uid uint32

	// group id of the file/directory ownership
	Gid uint32

	// dirent type (file, directory, other)
	DirentType fuseutil.DirentType

	// handle to the file/directory
	Handle fuseops.HandleID

	// boolean variable to assess whether file has been cached locally
	Cache bool

	// time the variable was downloaded to the local cache
	CacheTime time.Time

	// time the metadata of the file was modified
	MetadataWriteTime time.Time

	// time the content of the file was modified
	ContentWriteTime time.Time

	// CREW lock for metadata
	MetadataLock *datastructures.CREWResource

	// CREW lock for content
	ContentLock *datastructures.CREWResource
}

func (info *FileInfo) AddChild(childName string, childInode fuseops.InodeID) {
	info.Children = append(info.Children, childInode)
	info.ChildrenIndexMap[childName] = len(info.Children) - 1
}

/*
NewFileInfo method to create a new FileInfo
*/
func NewFileInfo(name string, path string, inode fuseops.InodeID, parent fuseops.InodeID, direntType fuseutil.DirentType) FileInfo {
	return FileInfo{
		Name:             name,
		Children:         make([]fuseops.InodeID, 0),
		ChildrenIndexMap: make(map[string]int),
		Parent:           parent,
		Inode:            inode,
		Path:             path,
		DirentType:       direntType,
		Cache:            false,
		CacheTime:        time.Unix(0, 0),
		ContentWriteTime: time.Now(),
	}
}

/*
NewSafeFileInfo method to create a new FileInfo
  - also initializes CREW locks
*/
func NewSafeFileInfo(name string, path string, inode fuseops.InodeID, parent fuseops.InodeID, direntType fuseutil.DirentType) FileInfo {
	return FileInfo{
		Name:              name,
		Children:          make([]fuseops.InodeID, 0),
		ChildrenIndexMap:  make(map[string]int),
		Parent:            parent,
		Inode:             inode,
		Path:              path,
		DirentType:        direntType,
		Cache:             false,
		CacheTime:         time.Unix(0, 0),
		ContentWriteTime:  time.Now(),
		MetadataWriteTime: time.Now(),
		ContentLock:       datastructures.NewCREWResource(),
		MetadataLock:      datastructures.NewCREWResource(),
	}
}

/**
TODO
	- implement caching
		- read/write request
			- request contains cacheBool and cacheTime
			- permission granted for read iff cache is invalid
			- permission granted for write iff cache is valid
		- read/write ack
			- send inode in request
			- return new writecontent time
	- implement local handles
*/
