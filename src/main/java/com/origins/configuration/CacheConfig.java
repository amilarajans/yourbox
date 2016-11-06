/*
 * Copyright (c) 2016-2016, Origins Software and Web Solutions. All Rights Reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.origins.configuration;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.origins.enums.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

@SuppressWarnings("unused")
@Configuration
@EnableCaching
@EnableHazelcastHttpSession
@AutoConfigureAfter(value = {MetricsConfig.class})
public class CacheConfig {

    private static HazelcastInstance hazelcastInstance;
    private final Logger log = LoggerFactory.getLogger(CacheConfig.class);
    @Inject
    private Environment env;

    private CacheManager cacheManager;

    /**
     * @return the unique instance.
     */
    public static HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    @PreDestroy
    public void destroy() {
        log.info("Closing Cache Manager");
        Hazelcast.shutdownAll();
    }

    @Bean
    public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
        log.debug("Starting HazelcastCacheManager");
        cacheManager = new com.hazelcast.spring.cache.HazelcastCacheManager(hazelcastInstance);
        return cacheManager;
    }

    @Bean
    public ManagementCenterConfig centerConfig() {
        ManagementCenterConfig centerConfig = new ManagementCenterConfig();
        centerConfig.setEnabled(true);
        centerConfig.setUrl("http://localhost:9090/hz");
        return centerConfig;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(OriginsProperties originsProperties) {
        log.debug("Configuring Hazelcast");
        Config config = new Config();
        config.setInstanceName("Origins");

        config.setProperty("hazelcast.logging.type", "slf4j");

        config.getNetworkConfig().setPort(5701);
        config.getNetworkConfig().setPortAutoIncrement(true);

        // In development, remove multicast auto-configuration
        if (env.acceptsProfiles(Constants.SPRING_PROFILE_DEVELOPMENT)) {
            System.setProperty("hazelcast.local.localAddress", "127.0.0.1");

            config.getNetworkConfig().getJoin().getAwsConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getJoin().getTcpIpConfig().setEnabled(false);
        }


        config.getMapConfigs().put("default", initializeDefaultMapConfig());
        config.getMapConfigs().put("io.github.jhipster.sample.domain.*", initializeDomainMapConfig(originsProperties));
        config.getMapConfigs().put("clustered-http-sessions", initializeClusteredSession(originsProperties));

        hazelcastInstance = HazelcastInstanceFactory.newHazelcastInstance(config);

        return hazelcastInstance;
    }

    private MapConfig initializeDefaultMapConfig() {
        MapConfig mapConfig = new MapConfig();

        /*
            Number of backups. If 1 is set as the backup-count for example,
            then all entries of the map will be copied to another JVM for
            fail-safety. Valid numbers are 0 (no backup), 1, 2, 3.
         */
        mapConfig.setBackupCount(0);

        /*
            Valid values are:
            NONE (no eviction),
            LRU (Least Recently Used),
            LFU (Least Frequently Used).
            NONE is the default.
         */
        mapConfig.setEvictionPolicy(EvictionPolicy.LRU);

        /*
            Maximum size of the map. When max size is reached,
            map is evicted based on the policy defined.
            Any integer between 0 and Integer.MAX_VALUE. 0 means
            Integer.MAX_VALUE. Default is 0.
         */
        mapConfig.setMaxSizeConfig(new MaxSizeConfig(0, MaxSizeConfig.MaxSizePolicy.USED_HEAP_SIZE));

        /*
            When max. size is reached, specified percentage of
            the map will be evicted. Any integer between 0 and 100.
            If 25 is set for example, 25% of the entries will
            get evicted.
         */
        mapConfig.setEvictionPercentage(25);

        return mapConfig;
    }

    private MapConfig initializeDomainMapConfig(OriginsProperties originsProperties) {
        MapConfig mapConfig = new MapConfig();

        mapConfig.setTimeToLiveSeconds(originsProperties.getCache().getTimeToLiveSeconds());
        return mapConfig;
    }

    private MapConfig initializeClusteredSession(OriginsProperties originsProperties) {
        MapConfig mapConfig = new MapConfig();

        mapConfig.setBackupCount(originsProperties.getCache().getHazelcast().getBackupCount());
        mapConfig.setTimeToLiveSeconds(originsProperties.getCache().getTimeToLiveSeconds());
        return mapConfig;
    }

    /**
     * Use by Spring Security, to get events from Hazelcast.
     *
     * @return the session registry
     */
    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
}
