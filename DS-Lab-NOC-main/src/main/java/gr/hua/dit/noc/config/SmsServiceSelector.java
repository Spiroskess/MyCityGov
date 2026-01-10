package gr.hua.dit.noc.config;

import gr.hua.dit.noc.core.SmsService;
import gr.hua.dit.noc.core.impl.MockSmsService;
import gr.hua.dit.noc.core.impl.RouteeSmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Class (configuration) for selecting the instance of {@link SmsService} based on application properties.
 *
 * @author Dimitris Gkoulis
 */
@Configuration
public class SmsServiceSelector {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmsServiceSelector.class);

    @Bean
    public SmsService smsService(final RouteeProperties routeeProperties,
                                 final RouteeSmsService routeeSmsService,
                                 final MockSmsService mockSmsService) {
        if (routeeProperties == null) throw new NullPointerException();
        if (routeeSmsService == null) throw new NullPointerException();
        if (mockSmsService == null) throw new NullPointerException();

        if (StringUtils.hasText(routeeProperties.getAppId()) && StringUtils.hasText(routeeProperties.getAppSecret())) {
            LOGGER.info("RouteeSmsService is the default implementation of SmsService");
            return routeeSmsService;
        } else {
            LOGGER.info("MockSmsService is the default implementation of SmsService");
            return mockSmsService;
        }
    }
}
