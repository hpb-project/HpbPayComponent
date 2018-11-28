package io.hpb.pay.configure;

import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.hpb.web3.protocol.Web3;
import io.hpb.web3.protocol.Web3Service;
import io.hpb.web3.protocol.admin.Admin;
import io.hpb.web3.protocol.http.HttpService;
import io.hpb.web3.protocol.ipc.UnixIpcService;
import io.hpb.web3.protocol.ipc.WindowsIpcService;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Web3 auto configuration for Spring Boot.
 */
@Configuration
@ConditionalOnClass(Web3.class)
@EnableConfigurationProperties(Web3Properties.class)
public class Web3AutoConfiguration {

    private static Log log = LogFactory.getLog(Web3AutoConfiguration.class);

    @Autowired
    private Web3Properties properties;

    @Bean
    @ConditionalOnMissingBean
    public Web3 Web3() {
        Web3Service Web3Service = buildService(properties.getClientAddress());
        log.info("Building service for endpoint: " + properties.getClientAddress());
        return Web3.build(Web3Service);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = Web3Properties.WEB3_PREFIX, name = "admin-client", havingValue = "true")
    public Admin admin() {
        Web3Service Web3Service = buildService(properties.getClientAddress());
        log.info("Building admin service for endpoint: " + properties.getClientAddress());
        return Admin.build(Web3Service);
    }

    private Web3Service buildService(String clientAddress) {
        Web3Service Web3Service;

        if (clientAddress == null || clientAddress.equals("")) {
            Web3Service = new HttpService(createOkHttpClient());
        } else if (clientAddress.startsWith("http")) {
            Web3Service = new HttpService(clientAddress, createOkHttpClient(), false);
        } else if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            Web3Service = new WindowsIpcService(clientAddress);
        } else {
            Web3Service = new UnixIpcService(clientAddress);
        }

        return Web3Service;
    }

    private OkHttpClient createOkHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        configureLogging(builder);
        configureTimeouts(builder);
        return builder.build();
    }

    private void configureTimeouts(OkHttpClient.Builder builder) {
        Long tos = properties.getHttpTimeoutSeconds();
        if (tos != null) {
            builder.connectTimeout(tos, TimeUnit.SECONDS);
            builder.readTimeout(tos, TimeUnit.SECONDS);  // Sets the socket timeout too
            builder.writeTimeout(tos, TimeUnit.SECONDS);
        }
    }

    private static void configureLogging(OkHttpClient.Builder builder) {
        if (log.isDebugEnabled()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(log::debug);
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(logging);
        }
    }


   
}
