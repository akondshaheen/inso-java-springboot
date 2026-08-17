package com.inso.learning.taskflow.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * =============================================================================
 * ENABLING SPRING'S CACHING SUPPORT
 * =============================================================================
 *
 * WHAT DOES @EnableCaching ACTUALLY DO?
 * -------------------------------------------------------------------------
 * This annotation turns on Spring's caching machinery. Behind the scenes,
 * Spring creates a PROXY around every bean that has a method annotated
 * with @Cacheable or @CacheEvict (see TagService) - a generated stand-in
 * object that intercepts calls to that method. On a call to a @Cacheable
 * method, the proxy first checks whether a result is already stored for
 * the given arguments; if so, it returns the stored value immediately and
 * never runs the real method body at all (a "cache hit"). If not (a
 * "cache miss"), it lets the real method run, then stores its result
 * before returning it. This is the exact same PROXY-based interception
 * idea already used for @Transactional - Spring re-uses this general
 * mechanism (built on Java's dynamic proxies, or CGLIB subclassing when
 * there is no interface) for many of its declarative features.
 *
 * WHY IS THIS IN ITS OWN CLASS, INSTEAD OF ON TaskFlowApplication?
 * -------------------------------------------------------------------------
 * @WebMvcTest and @DataJpaTest (our narrow test slices) still treat
 * TaskFlowApplication as their "configuration source" when starting a
 * trimmed-down application context, which means any annotation placed
 * DIRECTLY on that class is still processed, even in those slices. Those
 * slices deliberately do NOT auto-configure a CacheManager bean (they
 * have no reason to - a controller test does not care about caching), so
 * @EnableCaching sitting on TaskFlowApplication would make those tests
 * fail to start with "no CacheManager bean available". Keeping
 * @EnableCaching on its own plain @Configuration class means it is only
 * picked up by a FULL application context (the real running application,
 * or a full @SpringBootTest) - test slices only scan for the specific
 * component types they care about (controllers, repositories, ...) and
 * simply never look at this class at all.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}
