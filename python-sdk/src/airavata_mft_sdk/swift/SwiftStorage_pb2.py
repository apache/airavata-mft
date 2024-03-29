# -*- coding: utf-8 -*-
# Generated by the protocol buffer compiler.  DO NOT EDIT!
# source: swift/SwiftStorage.proto
"""Generated protocol buffer code."""
from google.protobuf import descriptor as _descriptor
from google.protobuf import descriptor_pool as _descriptor_pool
from google.protobuf import message as _message
from google.protobuf import reflection as _reflection
from google.protobuf import symbol_database as _symbol_database
# @@protoc_insertion_point(imports)

_sym_db = _symbol_database.Default()




DESCRIPTOR = _descriptor_pool.Default().AddSerializedFile(b'\n\x18swift/SwiftStorage.proto\x12\x34org.apache.airavata.mft.resource.stubs.swift.storage\"R\n\x0cSwiftStorage\x12\x11\n\tstorageId\x18\x01 \x01(\t\x12\x11\n\tcontainer\x18\x02 \x01(\t\x12\x0c\n\x04name\x18\x03 \x01(\t\x12\x0e\n\x06region\x18\x05 \x01(\t\"8\n\x17SwiftStorageListRequest\x12\x0e\n\x06offset\x18\x01 \x01(\x05\x12\r\n\x05limit\x18\x02 \x01(\x05\"p\n\x18SwiftStorageListResponse\x12T\n\x08storages\x18\x01 \x03(\x0b\x32\x42.org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage\"+\n\x16SwiftStorageGetRequest\x12\x11\n\tstorageId\x18\x01 \x01(\t\"_\n\x19SwiftStorageCreateRequest\x12\x11\n\tstorageId\x18\x01 \x01(\t\x12\x11\n\tcontainer\x18\x02 \x01(\t\x12\x0c\n\x04name\x18\x03 \x01(\t\x12\x0e\n\x06region\x18\x05 \x01(\t\"_\n\x19SwiftStorageUpdateRequest\x12\x11\n\tstorageId\x18\x01 \x01(\t\x12\x11\n\tcontainer\x18\x02 \x01(\t\x12\x0c\n\x04name\x18\x03 \x01(\t\x12\x0e\n\x06region\x18\x05 \x01(\t\"/\n\x1aSwiftStorageUpdateResponse\x12\x11\n\tstorageId\x18\x01 \x01(\t\".\n\x19SwiftStorageDeleteRequest\x12\x11\n\tstorageId\x18\x01 \x01(\t\",\n\x1aSwiftStorageDeleteResponse\x12\x0e\n\x06status\x18\x01 \x01(\x08\x42\x02P\x01\x62\x06proto3')



_SWIFTSTORAGE = DESCRIPTOR.message_types_by_name['SwiftStorage']
_SWIFTSTORAGELISTREQUEST = DESCRIPTOR.message_types_by_name['SwiftStorageListRequest']
_SWIFTSTORAGELISTRESPONSE = DESCRIPTOR.message_types_by_name['SwiftStorageListResponse']
_SWIFTSTORAGEGETREQUEST = DESCRIPTOR.message_types_by_name['SwiftStorageGetRequest']
_SWIFTSTORAGECREATEREQUEST = DESCRIPTOR.message_types_by_name['SwiftStorageCreateRequest']
_SWIFTSTORAGEUPDATEREQUEST = DESCRIPTOR.message_types_by_name['SwiftStorageUpdateRequest']
_SWIFTSTORAGEUPDATERESPONSE = DESCRIPTOR.message_types_by_name['SwiftStorageUpdateResponse']
_SWIFTSTORAGEDELETEREQUEST = DESCRIPTOR.message_types_by_name['SwiftStorageDeleteRequest']
_SWIFTSTORAGEDELETERESPONSE = DESCRIPTOR.message_types_by_name['SwiftStorageDeleteResponse']
SwiftStorage = _reflection.GeneratedProtocolMessageType('SwiftStorage', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGE,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorage)
  })
_sym_db.RegisterMessage(SwiftStorage)

SwiftStorageListRequest = _reflection.GeneratedProtocolMessageType('SwiftStorageListRequest', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGELISTREQUEST,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageListRequest)
  })
_sym_db.RegisterMessage(SwiftStorageListRequest)

SwiftStorageListResponse = _reflection.GeneratedProtocolMessageType('SwiftStorageListResponse', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGELISTRESPONSE,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageListResponse)
  })
_sym_db.RegisterMessage(SwiftStorageListResponse)

SwiftStorageGetRequest = _reflection.GeneratedProtocolMessageType('SwiftStorageGetRequest', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGEGETREQUEST,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageGetRequest)
  })
_sym_db.RegisterMessage(SwiftStorageGetRequest)

SwiftStorageCreateRequest = _reflection.GeneratedProtocolMessageType('SwiftStorageCreateRequest', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGECREATEREQUEST,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageCreateRequest)
  })
_sym_db.RegisterMessage(SwiftStorageCreateRequest)

SwiftStorageUpdateRequest = _reflection.GeneratedProtocolMessageType('SwiftStorageUpdateRequest', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGEUPDATEREQUEST,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageUpdateRequest)
  })
_sym_db.RegisterMessage(SwiftStorageUpdateRequest)

SwiftStorageUpdateResponse = _reflection.GeneratedProtocolMessageType('SwiftStorageUpdateResponse', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGEUPDATERESPONSE,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageUpdateResponse)
  })
_sym_db.RegisterMessage(SwiftStorageUpdateResponse)

SwiftStorageDeleteRequest = _reflection.GeneratedProtocolMessageType('SwiftStorageDeleteRequest', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGEDELETEREQUEST,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageDeleteRequest)
  })
_sym_db.RegisterMessage(SwiftStorageDeleteRequest)

SwiftStorageDeleteResponse = _reflection.GeneratedProtocolMessageType('SwiftStorageDeleteResponse', (_message.Message,), {
  'DESCRIPTOR' : _SWIFTSTORAGEDELETERESPONSE,
  '__module__' : 'swift.SwiftStorage_pb2'
  # @@protoc_insertion_point(class_scope:org.apache.airavata.mft.resource.stubs.swift.storage.SwiftStorageDeleteResponse)
  })
_sym_db.RegisterMessage(SwiftStorageDeleteResponse)

if _descriptor._USE_C_DESCRIPTORS == False:

  DESCRIPTOR._options = None
  DESCRIPTOR._serialized_options = b'P\001'
  _SWIFTSTORAGE._serialized_start=82
  _SWIFTSTORAGE._serialized_end=164
  _SWIFTSTORAGELISTREQUEST._serialized_start=166
  _SWIFTSTORAGELISTREQUEST._serialized_end=222
  _SWIFTSTORAGELISTRESPONSE._serialized_start=224
  _SWIFTSTORAGELISTRESPONSE._serialized_end=336
  _SWIFTSTORAGEGETREQUEST._serialized_start=338
  _SWIFTSTORAGEGETREQUEST._serialized_end=381
  _SWIFTSTORAGECREATEREQUEST._serialized_start=383
  _SWIFTSTORAGECREATEREQUEST._serialized_end=478
  _SWIFTSTORAGEUPDATEREQUEST._serialized_start=480
  _SWIFTSTORAGEUPDATEREQUEST._serialized_end=575
  _SWIFTSTORAGEUPDATERESPONSE._serialized_start=577
  _SWIFTSTORAGEUPDATERESPONSE._serialized_end=624
  _SWIFTSTORAGEDELETEREQUEST._serialized_start=626
  _SWIFTSTORAGEDELETEREQUEST._serialized_end=672
  _SWIFTSTORAGEDELETERESPONSE._serialized_start=674
  _SWIFTSTORAGEDELETERESPONSE._serialized_end=718
# @@protoc_insertion_point(module_scope)
