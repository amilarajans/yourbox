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

import com.hazelcast.config.Config;
import com.hazelcast.config.ManagementCenterConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;


/**
 * Created by Amila-Kumara on 13/10/2016.
 */
@Configuration
@EnableCaching
@EnableHazelcastHttpSession
public class HazelcastConfig {

    @Bean
    public ManagementCenterConfig centerConfig() {
        ManagementCenterConfig centerConfig = new ManagementCenterConfig();
        centerConfig.setEnabled(true);
        centerConfig.setUrl("http://localhost:9090/hz");
        return centerConfig;
    }

    @Bean
    public Config config(ManagementCenterConfig centerConfig) {
        Config hazelcastConfig = new Config();
        hazelcastConfig.setManagementCenterConfig(centerConfig);
        hazelcastConfig.setProperty("hazelcast.logging.type", "slf4j");
        return hazelcastConfig;
//        return new Config().addMapConfig(
//                new MapConfig()
//                        .setName("accepted-messages")
//                        .setEvictionPolicy(EvictionPolicy.LRU)
//                        .setTimeToLiveSeconds(2400))
//                .setProperty("hazelcast.logging.type", "slf4j");
    }


    @Bean
    public HazelcastInstance embeddedHazelcast(Config hazelcastConfig) {
        return Hazelcast.newHazelcastInstance(hazelcastConfig);
    }
}