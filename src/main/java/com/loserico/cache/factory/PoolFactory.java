package com.loserico.cache.factory;

import com.loserico.cache.config.RedisProperties;
import com.loserico.json.resource.PropertyReader;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.util.Pool;

/**
 * Redis连接池工厂类，生成JedisPool 或者 ShardedJedisPool
 * <p>
 * Copyright: Copyright (c) 2019-10-17 13:52
 * <p>
 * Company: Sexy Uncle Inc.
 * <p>
 *
 * @author Rico Yu  ricoyu520@gmail.com
 * @version 1.0
 */
public interface PoolFactory {

	/**
	 * <pre>
	 * 参数名		含义										默认值			使用建议
	 * maxTotal		资源池中最大连接数							8				
	 * maxIdle		资源池允许最大空闲的连接数						8				
	 * minIdle		资源池确保最少空闲的连接数						0				
	 * testOnBorrow	向资源池借用连接时是否做连接有效性检测(ping)		false			业务量很大时候建议设置为false(多一次ping的开销)。
	 * 				无效连接会被移除
	 * testOnReturn	向资源池归还连接时是否做连接有效性检测(ping)		false			业务量很大时候建议设置为false(多一次ping的开销)。
	 * 				无效连接会被移除	
	 * jmxEnabled	是否开启jmx监控，可用于监控					true			建议开启，但应用本身也要开启
	 * blockWhenExhausted	
	 * 				当资源池用尽后，调用者是否要等待。				true			建议使用默认值
	 * 				只有当为true时，下面的maxWaitMillis才会生效	
	 * maxWaitMillis当资源池连接用尽后，调用者的最大等待时间(单位为毫秒)	-1：表示永不超时	不建议使用默认值
	 * testWhileIdle是否开启空闲资源监测							false			true
	 * timeBetweenEvictionRunsMillis	
	 * 				空闲资源的检测周期(单位为毫秒)					-1：不检测		建议设置，周期自行选择，也可以默认也可以使用下面JedisPoolConfig中的配置
	 * minEvictableIdleTimeMillis							    1000*60*30=30分钟	
	 * 				资源池中资源最小空闲时间(单位为毫秒)							可根据自身业务决定，大部分默认值即可，也可以考虑使用下面JeidsPoolConfig中的配置
	 * 				达到此值后空闲资源将被移除	
	 * numTestsPerEvictionRun	
	 * 				做空闲资源检测时，每次的采样数					3				可根据自身应用连接数进行微调,如果设置为-1，就是对所有连接做空闲监测
	 * </pre>
	 * @formatter:off
	 * @param propertyReader
	 * @return
	 */
	public default JedisPoolConfig config(PropertyReader propertyReader) {
		JedisPoolConfig config = new JedisPoolConfig();
		//最大连接数，如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
		config.setMaxTotal(propertyReader.getInt("redis.maxTotal", 400));
		//最大空闲数，控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
		config.setMaxIdle(propertyReader.getInt("redis.maxIdle", 100));
		//最小空闲数
		config.setMinIdle(propertyReader.getInt("redis.minIdle", 10));
		//是否在从池中取出连接前进行检验，如果检验失败，则从池中去除连接并尝试取出另一个
		config.setTestOnBorrow(propertyReader.getBoolean("redis.testOnBorrow", false));
		//在return给pool时，是否提前进行validate操作
		config.setTestOnReturn(propertyReader.getBoolean("redis.testOnReturn", false));
		//在空闲时检查有效性，默认false
		config.setTestWhileIdle(propertyReader.getBoolean("redis.testWhileIdle", false));
		//表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；
		//表示idle object evitor两次扫描之间要sleep的毫秒数
		config.setTimeBetweenEvictionRunsMillis(propertyReader.getInt("redis.timeBetweenEvictionRunsMillis", 60000));
		//这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
		config.setMinEvictableIdleTimeMillis(propertyReader.getInt("redis.minEvictableIdleTimeMillis", 30000));
		//表示idle object evitor每次扫描的最多的对象数
		config.setNumTestsPerEvictionRun(propertyReader.getInt("redis.numTestsPerEvictionRun", 1000));
		//等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
		config.setMaxWaitMillis(propertyReader.getInt("redis.maxWaitMillis", 3000));

		return config;
	}

	/**
	 * 根据RedisProperties创建JedisPoolConfig
	 * @param redisProperties
	 * @return JedisPoolConfig
	 */
	public default JedisPoolConfig config(RedisProperties redisProperties) {
		JedisPoolConfig config = new JedisPoolConfig();
		//最大连接数，如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
		config.setMaxTotal(redisProperties.getMaxTotal());
		//最大空闲数，控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
		config.setMaxIdle(redisProperties.getMaxIdle());
		//最小空闲数
		config.setMinIdle(redisProperties.getMinIdle());
		//是否在从池中取出连接前进行检验，如果检验失败，则从池中去除连接并尝试取出另一个
		config.setTestOnBorrow(redisProperties.isTestOnBorrow());
		//在return给pool时，是否提前进行validate操作
		config.setTestOnReturn(redisProperties.isTestOnReturn());
		//在空闲时检查有效性，默认false
		config.setTestWhileIdle(redisProperties.isTestWhileIdle());
		//表示一个对象至少停留在idle状态的最短时间，然后才能被idle object evitor扫描并驱逐；
		//表示idle object evitor两次扫描之间要sleep的毫秒数
		config.setTimeBetweenEvictionRunsMillis(redisProperties.getTimeBetweenEvictionRunsMillis());
		//这一项只有在timeBetweenEvictionRunsMillis大于0时才有意义
		config.setMinEvictableIdleTimeMillis(redisProperties.getMinEvictableIdleTimeMillis());
		//表示idle object evitor每次扫描的最多的对象数
		config.setNumTestsPerEvictionRun(redisProperties.getNumTestsPerEvictionRun());
		//等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
		config.setMaxWaitMillis(redisProperties.getMaxWaitMillis());
		
		return config;
	}

	/**
	 * 创建JedisPool
	 * @param propertyReader
	 * @return JedisPool
	 */
	public default Pool<Jedis> createPool(PropertyReader propertyReader) {
		return null;
	};

	/**
	 * 创建JedisPool
	 * @param redisProperties
	 * @return JedisPool
	 */
	public default Pool<Jedis> createPool(RedisProperties redisProperties) {
		return null;
	};
	
	/**
	 * 创建JedisCluster
	 * @param propertyReader
	 * @return
	 */
	public default JedisCluster createCluster(PropertyReader propertyReader) {
		return null;
	}
}
