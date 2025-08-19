package sftpfs

import (
	"fmt"
	"io"
	"os"
	"path"
	"sync"
	"time"

	"github.com/jacobsa/fuse"
	"github.com/jacobsa/fuse/fuseops"
	"github.com/jacobsa/fuse/fuseutil"

	"github.com/pkg/sftp"

	"golang.org/x/crypto/ssh"

	"mft-fs/abstractfs"
	"mft-fs/datastructures"
)

// todo: make generated inode persistent

type SftpClientConfig struct {
	Host     string
	Port     uint16
	Username string
	Password string
	// PrivateKeyPath string
	RemoteRoot string
}

type sftpFSManager struct {
	abstractfs.FSManager

	client *sftp.Client
	conn   *ssh.Client
	config *SftpClientConfig

	mutex        *sync.Mutex
	data         map[fuseops.InodeID]*abstractfs.FileInfo
	pathToInode  map[string]fuseops.InodeID
	length       uint64
	inodeCounter fuseops.InodeID
}

func NewSFTPFS(mountDir string, config *SftpClientConfig) *abstractfs.AbstractFS {
	return &abstractfs.AbstractFS{
		Manager:  newSftpFsManager(config),
		MountDir: mountDir,
		Cachable: true,
		CacheDir: ".",
	}
}

func newSftpFsManager(config *SftpClientConfig) *sftpFSManager {
	if config == nil {
		fmt.Println("config is nil")
		return nil
	}
	return &sftpFSManager{
		config:       config,
		mutex:        &sync.Mutex{},
		data:         make(map[fuseops.InodeID]*abstractfs.FileInfo),
		length:       1,
		inodeCounter: 1,
		pathToInode:  make(map[string]fuseops.InodeID),
	}
}

type parentChildPair struct {
	parent fuseops.InodeID
	child  string
}

func writeFileInfo(stat *os.FileInfo, statSys *sftp.FileStat, info *abstractfs.FileInfo) {

	// general metadata
	info.Size = uint64(statSys.Size)
	var direntType fuseutil.DirentType
	if (*stat).IsDir() {
		direntType = fuseutil.DT_Directory
	} else {
		direntType = fuseutil.DT_File
	}
	info.DirentType = direntType

	// permissions metadata
	info.Mode = os.FileMode(statSys.Mode)
	info.Uid = statSys.UID
	info.Gid = statSys.GID

	// timing metadata
	info.Atime = statSys.AccessTime()
	info.Mtime = statSys.ModTime()
	// info.Ctime = time.Unix(statSys.Ctimespec.Sec, statSys.Ctimespec.Nsec)
	// info.Crtime = time.Unix(statSys.Birthtimespec.Sec, statSys.Birthtimespec.Nsec)

}

func (manager *sftpFSManager) generateInode() fuseops.InodeID {
	output := manager.inodeCounter
	manager.inodeCounter++
	return output
}

func (manager *sftpFSManager) bfs() error {
	fringe := datastructures.NewQueue()

	fringe.Enqueue(&parentChildPair{
		parent: 0,
		child:  manager.config.RemoteRoot,
	})
	var current *parentChildPair
	manager.length = 0
	for !fringe.IsEmpty() {
		current = fringe.Dequeue().(*parentChildPair)
		var p string
		if current.parent == 0 {
			p = manager.config.RemoteRoot
		} else {
			p = path.Join(manager.data[current.parent].Path, current.child)
		}

		// get the current path
		stat, e := manager.client.Stat(p)
		if e != nil {
			return e
		}
		statSys := stat.Sys().(*sftp.FileStat)

		// variable declaration
		var inode fuseops.InodeID
		var info *abstractfs.FileInfo
		var ok bool

		// get the inode
		inode, ok = manager.pathToInode[p]
		if !ok {
			inode = manager.generateInode()
		}

		// get info object; create if does not exist
		info, ok = manager.data[inode]
		if !ok {
			i := abstractfs.NewFileInfo(current.child, p, inode, current.parent, fuseutil.DT_Unknown)
			info = &i
			manager.data[inode] = info
			manager.pathToInode[p] = inode
			if current.parent != 0 {
				manager.data[current.parent].AddChild(current.child, inode)
			}
		}

		writeFileInfo(&stat, statSys, info)

		if info.DirentType == fuseutil.DT_Directory {
			dir, err := manager.client.ReadDir(info.Path)
			if err != nil {
				return err
			}
			for _, d := range dir {
				fringe.Enqueue(&parentChildPair{
					parent: info.Inode,
					child:  d.Name(),
				})
			}
		}

		manager.length++
	}

	return nil
}

func (manager *sftpFSManager) Start() error {
	sshConfig := &ssh.ClientConfig{
		User: manager.config.Username,
		Auth: []ssh.AuthMethod{
			ssh.Password(manager.config.Password),
		},

		// todo: change in production
		HostKeyCallback: ssh.InsecureIgnoreHostKey(),
		Timeout:         10 * time.Second,
	}

	// Dial the SSH server.
	conn, err := ssh.Dial("tcp", fmt.Sprintf("%s:%d", manager.config.Host, manager.config.Port), sshConfig)
	if err != nil {
		return fmt.Errorf("failed to dial SSH server: %w", err)
	}

	// Create the SFTP client from the SSH connection.
	client, err := sftp.NewClient(conn)
	if err != nil {
		// If SFTP client creation fails, ensure the SSH connection is closed.
		conn.Close()
		return fmt.Errorf("failed to create SFTP client: %w", err)
	}

	// update manager attributes
	manager.conn = conn
	manager.client = client

	return manager.bfs()
}

func (manager *sftpFSManager) Teardown() error {
	var err error

	if manager.client != nil {
		err = manager.client.Close()
		if err != nil {
			return err
		}
	}

	if manager.conn != nil {
		err = manager.conn.Close()
		if err != nil {
			return err
		}
	}

	return nil
}

func (manager *sftpFSManager) GetSize() (uint64, error) {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()

	if manager.client == nil {
		return 0, fmt.Errorf("SFTP client is not initialized")
	}

	return 0, nil
	stat, e := manager.client.Stat(manager.config.RemoteRoot)
	if e != nil {
		return 0, e
	}
	return uint64(stat.Size()), nil
}

func (manager *sftpFSManager) GetLength() (uint64, error) {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()

	if manager.client == nil {
		return 0, fmt.Errorf("SFTP client is not initialized")
	}

	return manager.length, nil

}

func (manager *sftpFSManager) GetInfo(id fuseops.InodeID) (*abstractfs.FileInfo, error) {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()

	if manager.client == nil {
		return nil, fmt.Errorf("SFTP client is not initialized")
	}

	var e error

	// get the inode object
	info, ok := manager.data[id]
	if !ok {
		// if it does not exist, perform a BFS to get it
		e = manager.bfs()
		if e != nil {
			return nil, e
		}

		// if it still does not exist, throw does not exist error
		info, ok = manager.data[id]
		if !ok {
			return nil, fuse.ENOENT
		}
	}

	// fetch its statistics
	stat, e := manager.client.Stat(info.Path)
	if e != nil {
		// if it errors, perform a BFS to get latest info
		e = manager.bfs()
		if e != nil {
			return nil, e
		}

		info, ok = manager.data[id]
		// if it still errors, throw the error
		stat, e = manager.client.Stat(info.Path)
		if e != nil {
			return nil, e
		}
	}

	writeFileInfo(&stat, stat.Sys().(*sftp.FileStat), info)

	return info, nil
}

func (manager *sftpFSManager) SetInfo(id fuseops.InodeID, uidptr *uint32, gidptr *uint32, sizeptr *uint64, modeptr *os.FileMode, atimeptr *time.Time, mtimeptr *time.Time) error {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()

	if manager.client == nil {
		return fmt.Errorf("SFTP client is not initialized")
	}

	var e error

	info, ok := manager.data[id]
	if !ok {
		info, e = manager.GetInfo(id)
		if e != nil {
			return e
		}
	}

	if modeptr != nil {
		e = manager.client.Chmod(info.Path, *modeptr)
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
	e = manager.client.Chown(info.Path, uid, gid)
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
	e = manager.client.Chtimes(info.Path, atime, mtime)
	if e != nil {
		return e
	}

	return nil
}

func (manager *sftpFSManager) Delete(id fuseops.InodeID) error {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()
	if manager.client == nil {
		return fmt.Errorf("SFTP client is not initialized")
	}

	var e error
	info, ok := manager.data[id]
	if !ok {
		info, e = manager.GetInfo(id)
		if e != nil {
			return e
		}
	}
	return manager.client.Remove(info.Path)
}

func (manager *sftpFSManager) MkDir(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()
	if manager.client == nil {
		return 0, fmt.Errorf("SFTP client is not initialized")
	}
	var e error
	info, ok := manager.data[parent]
	if !ok {
		info, e = manager.GetInfo(parent)
		if e != nil {
			return 0, e
		}
	}
	p := path.Join(info.Path, name)

	// make the directory
	e = manager.client.Mkdir(p)
	if e != nil {
		return 0, e
	}

	// set permissions
	e = manager.client.Chmod(p, mode)
	if e != nil {
		return 0, e
	}

	// create a new inode in local representation
	stat, e := manager.client.Stat(p)
	if e != nil {
		return 0, e
	}
	statSys := stat.Sys().(*sftp.FileStat)
	inode := manager.generateInode()
	i := abstractfs.NewFileInfo(name, p, inode, parent, fuseutil.DT_Unknown)
	info = &i
	manager.data[inode] = info
	manager.pathToInode[p] = inode
	manager.data[parent].AddChild(name, inode)
	writeFileInfo(&stat, statSys, info)
	return inode, nil
}

func (manager *sftpFSManager) CreateFile(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()
	if manager.client == nil {
		return 0, fmt.Errorf("SFTP client is not initialized")
	}
	var e error
	info, ok := manager.data[parent]
	if !ok {
		info, e = manager.GetInfo(parent)
		if e != nil {
			return 0, e
		}
	}
	p := path.Join(info.Path, name)

	// make the directory
	_, e = manager.client.Create(p)
	if e != nil {
		return 0, e
	}

	// set permissions
	e = manager.client.Chmod(p, mode)
	if e != nil {
		return 0, e
	}

	// create a new inode in local representation
	stat, e := manager.client.Stat(p)
	if e != nil {
		return 0, e
	}
	statSys := stat.Sys().(*sftp.FileStat)
	inode := manager.generateInode()
	i := abstractfs.NewFileInfo(name, p, inode, parent, fuseutil.DT_Unknown)
	info = &i
	manager.data[inode] = info
	manager.pathToInode[p] = inode
	manager.data[parent].AddChild(name, inode)
	writeFileInfo(&stat, statSys, info)
	return inode, nil
}

func (manager *sftpFSManager) RmDir(id fuseops.InodeID) error {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()
	if manager.client == nil {
		return fmt.Errorf("SFTP client is not initialized")
	}

	var e error
	info, ok := manager.data[id]
	if !ok {
		info, e = manager.GetInfo(id)
		if e != nil {
			return e
		}
	}
	return manager.client.RemoveDirectory(info.Path)
}

func (manager *sftpFSManager) SyncFile(inode fuseops.InodeID) error {
	return nil
}

func (manager *sftpFSManager) WriteAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error) {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()
	if manager.client == nil {
		return 0, fmt.Errorf("SFTP client is not initialized")
	}

	var e error
	info, ok := manager.data[inode]
	if !ok {
		info, e = manager.GetInfo(inode)
		if e != nil {
			return 0, e
		}
	}

	sftpFile, e := manager.client.Open(info.Path)
	if e != nil {
		return 0, e
	}

	_, err = sftpFile.Seek(off, 0)
	if err != nil {
		return 0, err
	}

	n, err = sftpFile.Write(data)
	if err != nil {
		return 0, err
	}

	return n, nil
}

func (manager *sftpFSManager) ReadAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error) {
	manager.mutex.Lock()
	defer manager.mutex.Unlock()
	if manager.client == nil {
		return 0, fmt.Errorf("SFTP client is not initialized")
	}
	var e error
	info, ok := manager.data[inode]
	if !ok {
		info, e = manager.GetInfo(inode)
		if e != nil {
			return 0, e
		}
	}

	sftpFile, e := manager.client.Open(info.Path)
	if e != nil {
		return 0, e
	}

	_, err = sftpFile.Seek(off, 0)
	if err != nil {
		return 0, err
	}

	n, err = sftpFile.Read(data)
	if err != nil && err != io.EOF {
		return n, err
	}

	return n, nil
}

//func (manager *sftpFSManager) Destroy() error {
//	return nil
//}
