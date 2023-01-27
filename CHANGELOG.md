# Changelog

## [Unreleased](https://github.com/apache/airavata-mft/tree/HEAD)

[Full Changelog](https://github.com/apache/airavata-mft/compare/v0.0.1...HEAD)

**Implemented enhancements:**

- Provide directory copy functionality in the CLI [\#63](https://github.com/apache/airavata-mft/issues/63)
- Integrate CLI into Python SDK [\#62](https://github.com/apache/airavata-mft/issues/62)
- Expose file browsing API to global API  [\#60](https://github.com/apache/airavata-mft/issues/60)

**Fixed bugs:**

- gRPC installation in macs with arm64e \(M1/M2\) [\#71](https://github.com/apache/airavata-mft/issues/71)

**Merged pull requests:**

- standalone-service implementation [\#70](https://github.com/apache/airavata-mft/pull/70) ([lahirujayathilake](https://github.com/lahirujayathilake))
- GCS Transfer python CLI integration [\#68](https://github.com/apache/airavata-mft/pull/68) ([Jayancv](https://github.com/Jayancv))
- single-service Implementation [\#67](https://github.com/apache/airavata-mft/pull/67) ([lahirujayathilake](https://github.com/lahirujayathilake))
- Bump jackson-databind from 2.12.3 to 2.12.7.1 in /agent/service [\#66](https://github.com/apache/airavata-mft/pull/66) ([dependabot[bot]](https://github.com/apps/dependabot))
- Bump commons-net from 3.6 to 3.9.0 in /transport/ftp-transport [\#65](https://github.com/apache/airavata-mft/pull/65) ([dependabot[bot]](https://github.com/apps/dependabot))

## [v0.0.1](https://github.com/apache/airavata-mft/tree/v0.0.1) (2022-12-25)

[Full Changelog](https://github.com/apache/airavata-mft/compare/0.1-pre-release...v0.0.1)

**Fixed bugs:**

- Resource and Secret service has logging jar conflicts [\#42](https://github.com/apache/airavata-mft/issues/42)

**Merged pull requests:**

- Adding GCS service command-line support [\#64](https://github.com/apache/airavata-mft/pull/64) ([Jayancv](https://github.com/Jayancv))
- \[SECURITY\] Fix Temporary File Information Disclosure Vulnerability
 [\#59](https://github.com/apache/airavata-mft/pull/59) ([JLLeitschuh](https://github.com/JLLeitschuh))
- Bump jackson-databind from 2.10.0 to 2.12.7.1 in /agent [\#58](https://github.com/apache/airavata-mft/pull/58) ([dependabot[bot]](https://github.com/apps/dependabot))
- Fix sessionToken max length [\#51](https://github.com/apache/airavata-mft/pull/51) ([PatrickPradier](https://github.com/PatrickPradier))
- OData Download API Support [\#50](https://github.com/apache/airavata-mft/pull/50) ([DImuthuUpe](https://github.com/DImuthuUpe))
- Add docker mode [\#49](https://github.com/apache/airavata-mft/pull/49) ([PatrickPradier](https://github.com/PatrickPradier))

## [0.1-pre-release](https://github.com/apache/airavata-mft/tree/0.1-pre-release) (2022-03-16)

[Full Changelog](https://github.com/apache/airavata-mft/compare/2a1a4416ed9b4cae51895573d4f176102b93bc73...0.1-pre-release)

**Closed issues:**

- Allow downloading a path within a resource [\#38](https://github.com/apache/airavata-mft/issues/38)
- Is there any commercial support available for this project? [\#32](https://github.com/apache/airavata-mft/issues/32)
- Exposing directory metadata view through API [\#20](https://github.com/apache/airavata-mft/issues/20)
- Decouple Storage endpoint and resource definition in the resource API [\#19](https://github.com/apache/airavata-mft/issues/19)

**Merged pull requests:**

- fix-child-path-check [\#41](https://github.com/apache/airavata-mft/pull/41) ([machristie](https://github.com/machristie))
- Update Python stub generation docs to include CredCommon.proto [\#39](https://github.com/apache/airavata-mft/pull/39) ([machristie](https://github.com/machristie))
- corshandler [\#37](https://github.com/apache/airavata-mft/pull/37) ([machristie](https://github.com/machristie))
- getDirectoryResourceMetadata-filesize [\#36](https://github.com/apache/airavata-mft/pull/36) ([machristie](https://github.com/machristie))
- Log exception when SSH session creation fails [\#34](https://github.com/apache/airavata-mft/pull/34) ([machristie](https://github.com/machristie))
- Support dynamic client providers [\#31](https://github.com/apache/airavata-mft/pull/31) ([isururanawaka](https://github.com/isururanawaka))
- Secret service fix [\#30](https://github.com/apache/airavata-mft/pull/30) ([isururanawaka](https://github.com/isururanawaka))
- Fixes for refactored storages api [\#28](https://github.com/apache/airavata-mft/pull/28) ([machristie](https://github.com/machristie))
- Readme Update [\#14](https://github.com/apache/airavata-mft/pull/14) ([gkiran292](https://github.com/gkiran292))
- Feature/ftp transport - FTP Transport protocol PR [\#13](https://github.com/apache/airavata-mft/pull/13) ([gkiran292](https://github.com/gkiran292))
- Adding junk file paths to gitignore [\#10](https://github.com/apache/airavata-mft/pull/10) ([gkiran292](https://github.com/gkiran292))
- MFT Dropbox Transport implementation [\#9](https://github.com/apache/airavata-mft/pull/9) ([sharanya17410](https://github.com/sharanya17410))
- Implementation of GCP Transport [\#6](https://github.com/apache/airavata-mft/pull/6) ([aksrajvanshi](https://github.com/aksrajvanshi))
- AIRAVATA-3325: Implementation of box transport [\#5](https://github.com/apache/airavata-mft/pull/5) ([dinukadesilva](https://github.com/dinukadesilva))
- add Copyright [\#2](https://github.com/apache/airavata-mft/pull/2) ([InstallingB](https://github.com/InstallingB))
- NIO based file transport implementation [\#1](https://github.com/apache/airavata-mft/pull/1) ([isururanawaka](https://github.com/isururanawaka))
