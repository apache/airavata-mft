package safe_remotefscomms

import (
	"github.com/jacobsa/fuse/fuseops"
	"github.com/jacobsa/fuse/fuseutil"
	"google.golang.org/protobuf/types/known/timestamppb"
	"mft-fs/archive/abstractfs"
	"os"
)

func ConvertFromComm(input *FileInfoMsg, output *abstractfs.FileInfo) {
	output.Name = input.Name
	output.Path = input.Path
	output.Inode = fuseops.InodeID(input.Inode)
	for _, child := range input.Children {
		output.Children = append(output.Children, fuseops.InodeID(child))
	}
	for name, index := range input.ChildrenIndexMap {
		output.ChildrenIndexMap[name] = int(index)
	}
	output.Parent = fuseops.InodeID(input.Parent)
	output.Nlink = input.Nlink
	output.Size = input.Size
	output.Mode = os.FileMode(input.Mode)
	output.Atime = input.Atime.AsTime()
	output.Mtime = input.Mtime.AsTime()
	output.Ctime = input.Ctime.AsTime()
	output.Crtime = input.Crtime.AsTime()
	output.Uid = input.Uid
	output.Gid = input.Gid
	output.DirentType = fuseutil.DirentType(input.DirentType)
	output.Handle = fuseops.HandleID(input.Handle)
}

func ConvertToComm(input *abstractfs.FileInfo, output *FileInfoMsg) {
	output.Name = input.Name
	output.Path = input.Path
	output.Inode = uint64(input.Inode)
	for _, child := range input.Children {
		output.Children = append(output.Children, uint64(child))
	}
	for name, index := range input.ChildrenIndexMap {
		output.ChildrenIndexMap[name] = uint32(index)
	}
	output.Parent = uint64(input.Parent)
	output.Nlink = input.Nlink
	output.Size = input.Size
	output.Mode = uint32(input.Mode)
	output.Atime = timestamppb.New(input.Atime)
	output.Mtime = timestamppb.New(input.Mtime)
	output.Ctime = timestamppb.New(input.Ctime)
	output.Crtime = timestamppb.New(input.Crtime)
	output.Uid = input.Uid
	output.Gid = input.Gid
	output.DirentType = uint32(input.DirentType)
	output.Handle = uint64(input.Handle)
}
