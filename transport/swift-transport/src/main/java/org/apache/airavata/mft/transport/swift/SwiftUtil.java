/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.airavata.mft.transport.swift;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.apache.airavata.mft.credential.stubs.swift.SwiftSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftV2AuthSecret;
import org.apache.airavata.mft.credential.stubs.swift.SwiftV3AuthSecret;
import org.jclouds.ContextBuilder;
import org.jclouds.openstack.keystone.config.KeystoneProperties;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.SwiftApiMetadata;

// https://jclouds.apache.org/guides/openstack/
public class SwiftUtil {
    private ThreadLocal<Map<String, SwiftApi>> swiftApiCache = ThreadLocal.withInitial(() -> {
        Map<String, SwiftApi> map = new HashMap<>();
        return map;
    });

    private static SwiftUtil instance;

    private SwiftUtil(){}

    public static synchronized SwiftUtil getInstance() {
        if (instance == null) {
            synchronized (SwiftUtil.class) {
                if (instance == null) {
                    instance = new SwiftUtil();
                }
            }
        }
        return instance;
    }

    public void releaseSwiftApi(SwiftSecret swiftSecret) {

    }

    private String getSecretKey(SwiftSecret swiftSecret) {
        switch (swiftSecret.getSecretCase()) {
            case V2AUTHSECRET:
                SwiftV2AuthSecret v2AuthSecret = swiftSecret.getV2AuthSecret();
                return v2AuthSecret.getTenant() + v2AuthSecret.getUserName() + v2AuthSecret.getPassword();
            case V3AUTHSECRET:
                SwiftV3AuthSecret v3AuthSecret = swiftSecret.getV3AuthSecret();
                return v3AuthSecret.getTenantName() + v3AuthSecret.getProjectDomainName()
                        + v3AuthSecret.getUserDomainName() + v3AuthSecret.getUserName() + v3AuthSecret.getPassword();
        }
        return null;
    }
    public SwiftApi leaseSwiftApi(SwiftSecret swiftSecret) throws Exception {

        String secretKey = getSecretKey(swiftSecret);

        if (swiftApiCache.get().containsKey(secretKey)) {
            return swiftApiCache.get().get(secretKey);
        }

        SwiftApi swiftApi;

        //String provider = "openstack-swift";
        Properties overrides = new Properties();
        switch (swiftSecret.getSecretCase()) {
          case V2AUTHSECRET:
              SwiftV2AuthSecret v2AuthSecret = swiftSecret.getV2AuthSecret();
              overrides.put(KeystoneProperties.KEYSTONE_VERSION, "2");
              swiftApi = ContextBuilder.newBuilder(new SwiftApiMetadata())
                  .endpoint(swiftSecret.getEndpoint())
                  .credentials(v2AuthSecret.getTenant() + ":" + v2AuthSecret.getUserName(),
                      v2AuthSecret.getPassword())
                  .overrides(overrides)
                  .buildApi(SwiftApi.class);
              break;
          case V3AUTHSECRET:

              SwiftV3AuthSecret v3AuthSecret = swiftSecret.getV3AuthSecret();
              overrides.put(KeystoneProperties.KEYSTONE_VERSION, "3");
              if (!"".equals(v3AuthSecret.getTenantName())) {
                  overrides.put(KeystoneProperties.SCOPE, "project:" + v3AuthSecret.getTenantName());
              }

              if (!"".equals(v3AuthSecret.getProjectDomainName())) {
                  overrides.put(KeystoneProperties.PROJECT_DOMAIN_NAME, v3AuthSecret.getProjectDomainName());
              }

              swiftApi =  ContextBuilder.newBuilder(new SwiftApiMetadata())
                  .endpoint(swiftSecret.getEndpoint())
                  .credentials(v3AuthSecret.getUserDomainName() + ":" + v3AuthSecret.getUserName(),
                      v3AuthSecret.getPassword())
                  .overrides(overrides)
                  .buildApi(SwiftApi.class);
              break;
          default:
            throw new Exception("No v2 or v3 auth secret set");
        }

        swiftApiCache.get().put(secretKey, swiftApi);
        return swiftApi;
    }
}
