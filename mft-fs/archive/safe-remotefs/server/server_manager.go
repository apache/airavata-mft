package server

import (
	"context"
	"fmt"
	"github.com/jacobsa/fuse/fuseops"
	"google.golang.org/protobuf/types/known/timestamppb"
	safe_remotefscomms2 "mft-fs/archive/safe-remotefs/safe-remotefscomms"
	"os"
	"time"
)

type Server struct {
	safe_remotefscomms2.UnimplementedRemoteFSCommsServer
	manager *SafeOSFSManager
}

func NewServerHandler(root string) *Server {
	output := &Server{
		manager: NewSafeOSFSManager(root),
	}
	return output
}

func (server *Server) GetSizeComm(ctx context.Context, in *safe_remotefscomms2.Empty) (*safe_remotefscomms2.UintMsg, error) {
	fmt.Println("GetSizeComm")
	size, e := server.manager.GetSize()
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.UintMsg{Data: size}, nil
}

func (server *Server) GetLengthComm(ctx context.Context, in *safe_remotefscomms2.Empty) (*safe_remotefscomms2.UintMsg, error) {
	fmt.Println("GetLengthComm")
	length, e := server.manager.GetLength()
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.UintMsg{Data: length}, nil
}

func (server *Server) GetInfoComm(ctx context.Context, in *safe_remotefscomms2.UintMsg) (*safe_remotefscomms2.FileInfoMsg, error) {
	fmt.Println("GetInfoComm")
	info, e := server.manager.GetInfo(fuseops.InodeID(in.Data))
	if e != nil {
		return nil, e
	}
	output := &safe_remotefscomms2.FileInfoMsg{
		ChildrenIndexMap: make(map[string]uint32),
	}
	info.MetadataLock.RequestRead()
	safe_remotefscomms2.ConvertToComm(info, output)
	info.MetadataLock.AckRead()
	return output, nil
}

func (server *Server) SetInfoComm(ctx context.Context, in *safe_remotefscomms2.SetInfoParamsMsg) (*safe_remotefscomms2.Empty, error) {
	fmt.Println("SetInfoComm")
	var uidptr *uint32
	var gidptr *uint32
	var sizeptr *uint64
	var modeptr *os.FileMode
	var atimeptr *time.Time
	var mtimeptr *time.Time
	if in.Uid != -1 {
		var temp uint32 = uint32(in.Uid)
		uidptr = &temp
	} else {
		uidptr = nil
	}
	if in.Gid != -1 {
		var temp uint32 = uint32(in.Uid)
		gidptr = &temp
	} else {
		gidptr = nil
	}
	if in.Mode != -1 {
		var temp os.FileMode = os.FileMode(in.Mode)
		modeptr = &temp
	} else {
		modeptr = nil
	}
	if in.Size != -1 {
		var temp uint64 = uint64(in.Size)
		sizeptr = &temp
	} else {
		sizeptr = nil
	}
	if !in.Atime.AsTime().Equal(time.Time{}) {
		var temp time.Time = in.Atime.AsTime()
		atimeptr = &temp
	} else {
		atimeptr = nil
	}
	if !in.Mtime.AsTime().Equal(time.Time{}) {
		var temp time.Time = in.Mtime.AsTime()
		mtimeptr = &temp
	} else {
		mtimeptr = nil
	}
	e := server.manager.SetInfo(fuseops.InodeID(in.Inode), uidptr, gidptr, sizeptr, modeptr, atimeptr, mtimeptr)
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.Empty{}, nil
}

func (server *Server) DeleteComm(ctx context.Context, in *safe_remotefscomms2.UintMsg) (*safe_remotefscomms2.Empty, error) {
	fmt.Println("DeleteComm")
	e := server.manager.Delete(fuseops.InodeID(in.Data))
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.Empty{}, nil
}

func (server *Server) MkDirComm(ctx context.Context, in *safe_remotefscomms2.MkInodeMsg) (*safe_remotefscomms2.UintMsg, error) {
	fmt.Println("MkDirComm")
	inode, e := server.manager.MkDir(fuseops.InodeID(in.Parent), in.Name, os.FileMode(in.Mode))
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.UintMsg{Data: uint64(inode)}, nil
}

func (server *Server) CreateFileComm(ctx context.Context, in *safe_remotefscomms2.MkInodeMsg) (*safe_remotefscomms2.UintMsg, error) {
	fmt.Println("CreateFileComm")
	inode, e := server.manager.CreateFile(fuseops.InodeID(in.Parent), in.Name, os.FileMode(in.Mode))
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.UintMsg{Data: uint64(inode)}, nil
}

func (server *Server) RmDirComm(ctx context.Context, in *safe_remotefscomms2.UintMsg) (*safe_remotefscomms2.Empty, error) {
	fmt.Println("RmDirComm")
	e := server.manager.RmDir(fuseops.InodeID(in.Data))
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.Empty{}, nil
}

func (server *Server) SyncFileComm(ctx context.Context, in *safe_remotefscomms2.UintMsg) (*safe_remotefscomms2.Empty, error) {
	fmt.Println("SyncFileComm")
	e := server.manager.SyncFile(fuseops.InodeID(in.Data))
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.Empty{}, nil
}

func (server *Server) WriteAtComm(ctx context.Context, in *safe_remotefscomms2.WriteAtMsg) (*safe_remotefscomms2.UintMsg, error) {
	fmt.Println("WriteAtComm")
	n, _ := server.manager.WriteAt(fuseops.InodeID(in.Inode), in.Data, in.Off)
	return &safe_remotefscomms2.UintMsg{Data: uint64(n)}, nil
}

func (server *Server) ReadAtComm(ctx context.Context, in *safe_remotefscomms2.ReadAtMsg) (*safe_remotefscomms2.ReadAtResponseMsg, error) {
	fmt.Println("ReadAtComm")
	data := make([]byte, in.Size)
	n, _ := server.manager.ReadAt(fuseops.InodeID(in.Inode), data, in.Off)
	return &safe_remotefscomms2.ReadAtResponseMsg{
		Data: data,
		N:    int64(n),
	}, nil
}

func (server *Server) RequestReadComm(ctx context.Context, in *safe_remotefscomms2.RequestResourceMsg) (*safe_remotefscomms2.RequestResponseMsg, error) {
	fmt.Println("RequestReadComm")
	success, e := server.manager.RequestRead(fuseops.InodeID(in.Inode), in.Cache, in.CacheTime.AsTime())
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.RequestResponseMsg{Success: success}, nil
}

func (server *Server) RequestWriteComm(ctx context.Context, in *safe_remotefscomms2.RequestResourceMsg) (*safe_remotefscomms2.RequestResponseMsg, error) {
	fmt.Println("RequestWriteComm")
	success, e := server.manager.RequestWrite(fuseops.InodeID(in.Inode), in.Cache, in.CacheTime.AsTime())
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.RequestResponseMsg{Success: success}, nil
}

func (server *Server) AckReadComm(ctx context.Context, in *safe_remotefscomms2.UintMsg) (*safe_remotefscomms2.AckResponseMsg, error) {
	fmt.Println("AckReadComm")
	timestamp, e := server.manager.AckRead(fuseops.InodeID(in.Data))
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.AckResponseMsg{WriteTime: timestamppb.New(timestamp)}, nil
}

func (server *Server) AckWriteComm(ctx context.Context, in *safe_remotefscomms2.UintMsg) (*safe_remotefscomms2.AckResponseMsg, error) {
	fmt.Println("AckWriteComm")
	timestamp, e := server.manager.AckWrite(fuseops.InodeID(in.Data))
	if e != nil {
		return nil, e
	}
	return &safe_remotefscomms2.AckResponseMsg{WriteTime: timestamppb.New(timestamp)}, nil
}
