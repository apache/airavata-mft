from rich import print
from pick import pick
import typer
from airavata_mft_sdk import mft_client
from airavata_mft_sdk.s3 import S3Credential_pb2
from airavata_mft_sdk.s3 import S3Storage_pb2
from airavata_mft_sdk import MFTTransferApi_pb2
from airavata_mft_sdk import MFTAgentStubs_pb2
from airavata_mft_sdk.common import StorageCommon_pb2
import configparser
import os

def handle_add_storage():

    session_token = ""
    aws_regions = ["us-east-2", "us-east-1", "us-west-1", "us-west-2", "af-south-1", "ap-east-1", "ap-east-1",
                   "ap-southeast-3", "ap-south-1", "ap-northeast-3", "ap-northeast-2", "ap-southeast-1",
                   "ap-southeast-2", "ap-northeast-1", "ca-central-1", "cn-north-1", "cn-northwest-1", "eu-central-1",
                   "eu-west-1", "eu-west-2", "eu-south-1", "eu-west-3", "eu-north-1", "eu-south-2", "eu-central-2",
                   "sa-east-1", "me-south-1", "me-central-1", "us-gov-east-1", "us-gov-west-1"]

    options = ["Through AWS Cli config file", "Enter manually" ]
    option, index = pick(options, "How do you want to load credentials", indicator="=>")

    if index == 1: # Manual configuration
        client_id = typer.prompt("Access Key ID")
        client_secret = typer.prompt("Secret Access Key")
        has_session_token = typer.confirm("Do you have a session token?", False)
        if has_session_token:
            session_token = typer.prompt("Session Token")

        is_aws = typer.confirm("Is this an AWS S3 bucket?", True)

        if is_aws:
            region, index = pick(aws_regions, "Select the AWS Region", indicator="=>")
            endpoint = "https://s3." + region + ".amazonaws.com"

        else: # If endpoint is a S3 compatible endpoint
            endpoint = typer.prompt("What is the S3 endpoint URL?")
            region = typer.prompt("What is the region of the bucket?")

    else: # Loading credentials from the aws cli config file
        config = configparser.RawConfigParser()
        path = os.path.join(os.path.expanduser('~'), '.aws/credentials')
        config.read(path)
        cred_sections = config.sections()

        if len(cred_sections) > 1:
            section, index = pick(cred_sections, "Which section do you need to use?", indicator="=>")
        elif len(cred_sections) == 1:
            section = cred_sections[0]
        else:
            print("No credential found in ~/.aws/credentials file")
            exit()

        client_id = config.get(section, 'aws_access_key_id')
        client_secret = config.get(section, 'aws_secret_access_key')
        if config.has_option(section, 'aws_session_token'):
            session_token =  config.get(section, 'aws_session_token')

        region, index = pick(aws_regions, "Select the AWS Region", indicator="=>")
        endpoint = "https://s3." + region + ".amazonaws.com"

    client = mft_client.MFTClient(transfer_api_port = 7003,
                                  transfer_api_secured = False,
                                  resource_service_host = "localhost",
                                  resource_service_port = 7003,
                                  resource_service_secured = False,
                                  secret_service_host = "localhost",
                                  secret_service_port = 7003)

    s3_secret = S3Credential_pb2.S3Secret(accessKey=client_id, secretKey=client_secret, sessionToken = session_token)
    secret_wrapper = MFTAgentStubs_pb2.SecretWrapper(s3=s3_secret)

    s3_storage = S3Storage_pb2.S3Storage(endpoint=endpoint, region=region)
    storage_wrapper = MFTAgentStubs_pb2.StorageWrapper(s3=s3_storage)

    direct_req = MFTAgentStubs_pb2.GetResourceMetadataRequest(resourcePath="", secret=secret_wrapper, storage=storage_wrapper)
    resource_medata_req = MFTTransferApi_pb2.FetchResourceMetadataRequest(directRequest = direct_req)
    metadata_resp = client.transfer_api.resourceMetadata(resource_medata_req)

    bucket_options = ["Manually Enter"]

    bucket_list = metadata_resp.directory.directories
    if len(bucket_list) > 0:
        for b in bucket_list:
            bucket_options.append(b.friendlyName)

    title = "Select the Bucket: "
    selected_bucket, index = pick(bucket_options, title, indicator="=>")
    if index == 0:
        selected_bucket = typer.prompt("Enter bucket name ")
    storage_name = typer.prompt("Name of the storage ", selected_bucket)

    s3_storage_create_req = S3Storage_pb2.S3StorageCreateRequest(
        endpoint=endpoint, region=region, bucketName=selected_bucket, name =storage_name)

    created_storage = client.s3_storage_api.createS3Storage(s3_storage_create_req)

    secret_create_req= S3Credential_pb2.S3SecretCreateRequest(accessKey=client_id, secretKey=client_secret)
    created_secret = client.s3_secret_api.createS3Secret(secret_create_req)

    secret_for_storage_req = StorageCommon_pb2.SecretForStorage(storageId = created_storage.storageId,
                                       secretId = created_secret.secretId,
                                       storageType = StorageCommon_pb2.StorageType.S3)

    client.common_api.registerSecretForStorage(secret_for_storage_req)

    print("Successfully added the S3 Bucket...")
