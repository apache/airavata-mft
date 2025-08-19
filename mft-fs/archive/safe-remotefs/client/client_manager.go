package client

import (
	"context"
	"crypto/rand"
	"errors"
	"fmt"
	"github.com/jacobsa/fuse/fuseops"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/types/known/timestamppb"
	"mft-fs/archive/abstractfs"
	safe_remotefscomms2 "mft-fs/archive/safe-remotefs/safe-remotefscomms"
	"os"
	"time"
)

type cacheInfo struct {
	cache     bool
	cacheTime time.Time
	handle    fuseops.HandleID
}

func minimum(a int, b int) int {
	if a < b {
		return a
	}
	return b
}

func newCacheInfo() *cacheInfo {
	return &cacheInfo{
		cache:     false,
		cacheTime: time.Unix(0, 0),
		handle:    fuseops.HandleID(0),
	}
}

type ClientManager struct {
	abstractfs.FSManager
	client        safe_remotefscomms2.RemoteFSCommsClient
	cachePath     string
	localInfo     map[fuseops.InodeID]*cacheInfo
	handleToInode map[fuseops.HandleID]fuseops.InodeID
}

func NewClientManager(conn *grpc.ClientConn, cachePath string) (*ClientManager, error) {
	output := &ClientManager{
		cachePath:     cachePath,
		localInfo:     make(map[fuseops.InodeID]*cacheInfo),
		handleToInode: make(map[fuseops.HandleID]fuseops.InodeID),
	}
	output.client = safe_remotefscomms2.NewRemoteFSCommsClient(conn)
	return output, nil
}

func (manager *ClientManager) GetSize() (uint64, error) {
	resp, e := manager.client.GetSizeComm(context.Background(), &safe_remotefscomms2.Empty{})
	if e != nil {
		return 0, e
	}
	return resp.Data, nil
}

func (manager *ClientManager) GetLength() (uint64, error) {
	resp, e := manager.client.GetLengthComm(context.Background(), &safe_remotefscomms2.Empty{})
	if e != nil {
		return 0, e
	}
	return resp.Data, nil
}

func (manager *ClientManager) GetInfo(inode fuseops.InodeID) (*abstractfs.FileInfo, error) {
	resp, e := manager.client.GetInfoComm(context.Background(), &safe_remotefscomms2.UintMsg{
		Data: uint64(inode),
	})
	if e != nil {
		return nil, e
	}
	output := &abstractfs.FileInfo{
		ChildrenIndexMap: make(map[string]int),
	}
	safe_remotefscomms2.ConvertFromComm(resp, output)
	info, ok := manager.localInfo[inode]
	if ok {
		output.Handle = info.handle
	}
	return output, nil
}

func (manager *ClientManager) SetInfo(inode fuseops.InodeID, uidptr *uint32, gidptr *uint32, sizeptr *uint64, modeptr *os.FileMode, atimeptr *time.Time, mtimeptr *time.Time) error {
	request := &safe_remotefscomms2.SetInfoParamsMsg{
		Inode: uint64(inode),
		Uid:   -1,
		Gid:   -1,
		Size:  -1,
		Mode:  -1,
		Atime: timestamppb.New(time.Time{}),
		Mtime: timestamppb.New(time.Time{}),
	}
	if uidptr != nil {
		request.Uid = int32(*uidptr)
	}
	if gidptr != nil {
		request.Gid = int32(*gidptr)
	}
	if sizeptr != nil {
		request.Size = int64(*sizeptr)
	}
	if modeptr != nil {
		request.Mode = int32(*modeptr)
	}
	if atimeptr != nil {
		request.Atime = timestamppb.New(*atimeptr)
	}
	if mtimeptr != nil {
		request.Mtime = timestamppb.New(*mtimeptr)
	}
	_, e := manager.client.SetInfoComm(context.Background(), request)
	return e
}

func (manager *ClientManager) Delete(inode fuseops.InodeID) error {
	_, e := manager.client.DeleteComm(context.Background(), &safe_remotefscomms2.UintMsg{Data: uint64(inode)})
	return e
}

func (manager *ClientManager) MkDir(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	resp, e := manager.client.MkDirComm(context.Background(), &safe_remotefscomms2.MkInodeMsg{
		Parent: uint64(parent),
		Name:   name,
		Mode:   uint32(mode),
	})
	if e != nil {
		return 0, e
	}
	return fuseops.InodeID(resp.Data), nil
}

func (manager *ClientManager) GenerateHandle(inode fuseops.InodeID) (fuseops.HandleID, error) {
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
	_, ok := manager.handleToInode[handle]
	if ok {
		return manager.GenerateHandle(inode)
	}
	info, ok := manager.localInfo[inode]
	if !ok {
		manager.localInfo[inode] = newCacheInfo()
		info = manager.localInfo[inode]
	}
	info.handle = handle
	manager.handleToInode[handle] = inode
	return handle, nil
}
func (manager *ClientManager) CreateFile(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	resp, e := manager.client.CreateFileComm(context.Background(), &safe_remotefscomms2.MkInodeMsg{
		Parent: uint64(parent),
		Name:   name,
		Mode:   uint32(mode),
	})
	if e != nil {
		return 0, e
	}
	return fuseops.InodeID(resp.Data), nil
}

func (manager *ClientManager) RmDir(inode fuseops.InodeID) error {
	_, e := manager.client.RmDirComm(context.Background(), &safe_remotefscomms2.UintMsg{Data: uint64(inode)})
	return e
}

func (manager *ClientManager) DeleteHandle(handle fuseops.HandleID) error {
	delete(manager.handleToInode, handle)
	return nil
}

func (manager *ClientManager) SyncFile(inode fuseops.InodeID) error {
	_, e := manager.client.SyncFileComm(context.Background(), &safe_remotefscomms2.UintMsg{Data: uint64(inode)})
	return e
}

func (manager *ClientManager) WriteAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error) {

	info, ok := manager.localInfo[inode]
	if !ok {
		fmt.Println("cache map issue")
		return 0, errors.New("local cache not updated")
	}

	msg := safe_remotefscomms2.RequestResourceMsg{
		CacheTime: timestamppb.New(info.cacheTime),
		Inode:     uint64(inode),
		Cache:     info.cache,
	}

	requestResp, e := manager.client.RequestWriteComm(context.Background(), &msg)
	if e != nil {
		return 0, e
	}

	if requestResp.Success {
		resp, e := manager.client.WriteAtComm(context.Background(), &safe_remotefscomms2.WriteAtMsg{
			Inode: uint64(inode),
			Data:  data,
			Off:   off,
		})
		if e != nil {
			return 0, e
		}

		ackResp, e := manager.client.AckWriteComm(context.Background(), &safe_remotefscomms2.UintMsg{Data: uint64(inode)})
		if e != nil {
			return 0, e
		}

		manager.localInfo[inode].cache = true
		manager.localInfo[inode].cacheTime = ackResp.WriteTime.AsTime()

		file, e := os.OpenFile(fmt.Sprintf("%s/file%d.txt", manager.cachePath, int(inode)), os.O_RDWR|os.O_CREATE, 777)
		if e != nil {
			return 0, e
		}
		_, e = file.WriteAt(data, off)
		if e != nil {
			return 0, e
		}

		return int(resp.Data), nil
	}

	fmt.Println("cache timing issue")
	return 0, errors.New("local cache not updated")
}
func (manager *ClientManager) ReadAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error) {

	msg := safe_remotefscomms2.RequestResourceMsg{
		CacheTime: timestamppb.New(time.Unix(0, 0)),
		Inode:     uint64(inode),
		Cache:     false,
	}

	info, ok := manager.localInfo[inode]
	if ok {
		msg.Cache = info.cache
		msg.CacheTime = timestamppb.New(info.cacheTime)
	} else {
		manager.localInfo[inode] = newCacheInfo()
	}

	requestResp, e := manager.client.RequestReadComm(context.Background(), &msg)
	if e != nil {
		return 0, e
	}

	if requestResp.Success {
		resp, e := manager.client.ReadAtComm(context.Background(), &safe_remotefscomms2.ReadAtMsg{
			Inode: uint64(inode),
			Size:  int64(len(data)),
			Off:   off,
		})
		if e != nil {
			return 0, e
		}
		ackResp, e := manager.client.AckReadComm(context.Background(), &safe_remotefscomms2.UintMsg{Data: uint64(inode)})
		if e != nil {
			return 0, e
		}
		manager.localInfo[inode].cacheTime = ackResp.WriteTime.AsTime()
		manager.localInfo[inode].cache = true
		for i := 0; i < minimum(len(data), len(resp.Data)); i++ {
			data[i] = resp.Data[i]
		}
		file, e := os.OpenFile(fmt.Sprintf("%s/file%d.txt", manager.cachePath, int(inode)), os.O_RDWR|os.O_CREATE, 777)
		if e != nil {
			return 0, e
		}
		_, e = file.WriteAt(data, off)
		if e != nil {
			return 0, e
		}
		return int(resp.N), nil
	} else {
		file, e := os.OpenFile(fmt.Sprintf("%s/file%d.txt", manager.cachePath, int(inode)), os.O_RDWR, 777)
		if e != nil {
			return 0, e
		}
		n, _ := file.ReadAt(data, off)
		return n, nil
	}
}

func (manager *ClientManager) Destroy() error {
	return nil
}
