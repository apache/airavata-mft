package client

import (
	"context"
	"github.com/jacobsa/fuse/fuseops"
	"google.golang.org/grpc"
	"google.golang.org/protobuf/types/known/timestamppb"
	"mft-fs/archive/abstractfs"
	remotefscomms2 "mft-fs/archive/remotefs/remotefscomms"
	"os"
	"time"
)

type ClientManager struct {
	abstractfs.FSManager
	client remotefscomms2.RemoteFSCommsClient
}

func NewClientManager(conn *grpc.ClientConn) (*ClientManager, error) {
	output := &ClientManager{}
	output.client = remotefscomms2.NewRemoteFSCommsClient(conn)
	return output, nil
}

func (manager *ClientManager) GetSize() (uint64, error) {
	resp, e := manager.client.GetSizeComm(context.Background(), &remotefscomms2.Empty{})
	if e != nil {
		return 0, e
	}
	return resp.Data, nil
}

func (manager *ClientManager) GetLength() (uint64, error) {
	resp, e := manager.client.GetLengthComm(context.Background(), &remotefscomms2.Empty{})
	if e != nil {
		return 0, e
	}
	return resp.Data, nil
}

func (manager *ClientManager) GetInfo(inode fuseops.InodeID) (*abstractfs.FileInfo, error) {
	resp, e := manager.client.GetInfoComm(context.Background(), &remotefscomms2.UintMsg{
		Data: uint64(inode),
	})
	if e != nil {
		return nil, e
	}
	output := &abstractfs.FileInfo{
		ChildrenIndexMap: make(map[string]int),
	}
	remotefscomms2.ConvertFromComm(resp, output)
	return output, nil
}

func (manager *ClientManager) SetInfo(inode fuseops.InodeID, uidptr *uint32, gidptr *uint32, sizeptr *uint64, modeptr *os.FileMode, atimeptr *time.Time, mtimeptr *time.Time) error {
	request := &remotefscomms2.SetInfoParamsMsg{
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
	_, e := manager.client.DeleteComm(context.Background(), &remotefscomms2.UintMsg{Data: uint64(inode)})
	return e
}

func (manager *ClientManager) MkDir(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	resp, e := manager.client.MkDirComm(context.Background(), &remotefscomms2.MkInodeMsg{
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
	resp, e := manager.client.GenerateHandleComm(context.Background(), &remotefscomms2.UintMsg{Data: uint64(inode)})
	if e != nil {
		return 0, e
	}
	return fuseops.HandleID(resp.Data), nil
}
func (manager *ClientManager) CreateFile(parent fuseops.InodeID, name string, mode os.FileMode) (fuseops.InodeID, error) {
	resp, e := manager.client.CreateFileComm(context.Background(), &remotefscomms2.MkInodeMsg{
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
	_, e := manager.client.RmDirComm(context.Background(), &remotefscomms2.UintMsg{Data: uint64(inode)})
	return e
}

func (manager *ClientManager) DeleteHandle(handle fuseops.HandleID) error {
	_, e := manager.client.DeleteHandleComm(context.Background(), &remotefscomms2.UintMsg{Data: uint64(handle)})
	return e
}

func (manager *ClientManager) SyncFile(inode fuseops.InodeID) error {
	_, e := manager.client.SyncFileComm(context.Background(), &remotefscomms2.UintMsg{Data: uint64(inode)})
	return e
}

func (manager *ClientManager) WriteAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error) {
	resp, e := manager.client.WriteAtComm(context.Background(), &remotefscomms2.ContentMsg{
		Inode: uint64(inode),
		Data:  data,
		Off:   off,
	})
	if e != nil {
		return 0, e
	}
	return int(resp.Data), nil
}
func (manager *ClientManager) ReadAt(inode fuseops.InodeID, data []byte, off int64) (n int, err error) {
	resp, e := manager.client.ReadAtComm(context.Background(), &remotefscomms2.ContentMsg{
		Inode: uint64(inode),
		Data:  data,
		Off:   off,
	})
	if e != nil {
		return 0, e
	}
	return int(resp.Data), nil
}

func (manager *ClientManager) Destroy() error {
	return nil
}
