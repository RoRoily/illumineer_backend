package com.buaa01.illumineer_backend.tool;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.buaa01.illumineer_backend.config.RedisConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisTool {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    //默认的存活时间：60*60=3600
    public static final long REDIS_DEFAULT_EXPIRE_TIME = 60 * 60;
    //设置默认的时间单位：秒，也就是说所有的Redis相关时间操作单位都是“秒”
    public static final TimeUnit REDIS_DEFAULT_EXPIRE_TIME_UNIT = TimeUnit.SECONDS;

    //---------------------以下是对于键key的相关操作--------------------

    /**
     * 设置指定的key的存活时间，单位：秒
     *
     * @param key        键
     * @param expireTime 存活时间（秒）
     */
    public void setExpire(String key, long expireTime) {
        redisTemplate.expire(key, expireTime, REDIS_DEFAULT_EXPIRE_TIME_UNIT);
    }
    /**
     * 根据键返回它的剩余的存活时间，单位：秒
     *
     * @param key 键
     * @return 剩余的存活时间，如果键不存在则返回-1
     */
    public long getExpire(String key) {
        Long expireTime = redisTemplate.getExpire(key, REDIS_DEFAULT_EXPIRE_TIME_UNIT);
        if (expireTime == null) return -1;
        else return expireTime;
    }

    /**
     * 设置过期日期
     *
     * @param key        键
     * @param expireDate 过期的日期
     * @return 是否成功执行
     */
    public Boolean setExpireDate(String key, Date expireDate) {
        return Boolean.TRUE.equals(redisTemplate.expireAt(key, expireDate));
    }

    /**
     * 移除过期时间，key将永久存在于缓存
     *
     * @param key 键
     * @return 操作是否成功
     */
    public Boolean persist(String key) {
        return Boolean.TRUE.equals(redisTemplate.persist(key));
    }

    /**
     * 删除指定key的缓存
     *
     * @param key 键
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除键
     *
     * @param keys 键的集合，set,list什么都行
     */
    public void deleteKeys(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 获取指定前缀的所有的key
     *
     * @param keyPrefix 前缀
     * @return 所有前缀符合的key的集合
     */
    public Set<String> getKeysByPrefix(String keyPrefix) {
        return redisTemplate.keys(keyPrefix + "*");
    }

    /**
     * 删除指定前缀的所有key的缓存
     *
     * @param keyPrefix 前缀
     */
    public void deleteByPrefix(String keyPrefix) {
        Set<String> keys = redisTemplate.keys(keyPrefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    /**
     * 查询key是否存在于redis
     *
     * @param key 键
     * @return boolean，是否存在
     */
    public Boolean isExist(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 序列化key
     *
     * @param key 键
     * @return 序列化后的字节数组
     */
    public byte[] dump(String key) {
        return redisTemplate.dump(key);
    }

    /**
     * 从当前数据库中随机返回一个 key
     *
     * @return 随机的 key
     */
    public String getRandomKey() {
        return redisTemplate.randomKey();
    }

    /**
     * 修改 key 的名称
     *
     * @param oldKey 原 key 名称
     * @param newKey 新 key 名称
     */
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 仅当 newKey 不存在时，将 oldKey 改名为 newKey
     *
     * @param oldKey 原 key 名称
     * @param newKey 新 key 名称
     * @return 是否成功
     */
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 返回 key 所储存的值的类型
     *
     * @param key key
     * @return 数据类型
     */
    public DataType type(String key) {
        return redisTemplate.type(key);
    }

    //--------------------键key相关操作结束--------------------

    //--------------------有序集合ZSet相关操作开始--------------------

    //定义ZSet相关的数据类，写入redis的ZSet应当使用这两种数据
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZSetTime {
        private Object object;
        private Date time;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ZSetScore {
        private Object object;
        private Double score;
    }


    /**
     * 按照分数，从小到大取排行榜，默认情况下redis按照从小到大排序存储
     *
     * @param key   键
     * @param start 开始位置，注意，集合中第一个元素的位置是0，和数组一致
     * @param end   结束位置
     *              <p>
     *              start 和 end 参数可以为负数，这表示从集合尾部开始的索引，例如 -1 表示最后一个元素，-2 表示倒数第二个元素，以此类推。
     * @return Object集合，根据顺序排出来的
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 按照分数，从大到小取排行榜
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 根据顺序排序的Object集合
     */
    public Set<Object> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 按分数从大到小取数据，携带分数
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 从大到小排序的ZSetScore List
     */
    public List<ZSetScore> reverseRangeWithScores(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (result == null) return null;
        List<ZSetScore> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : result) {
            list.add(new ZSetScore(tuple.getValue(), tuple.getScore()));
        }
        return list;
    }

    /**
     * 按分数从大到小取数据，携带时间（如果分数就是时间，使用这个）
     *
     * @param key   键
     * @param start 开始位置
     * @param end   结束位置
     * @return 从大到小排序的ZSetScore List
     */
    public List<ZSetTime> reverseRangeWithTimes(String key, long start, long end) {
        Set<ZSetOperations.TypedTuple<Object>> result = redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
        if (result == null) return null;
        List<ZSetTime> list = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> tuple : result) {
            list.add(new ZSetTime(tuple.getValue(), new Date(Objects.requireNonNull(tuple.getScore()).longValue())));
        }
        return list;
    }

    /**
     * 按照分数，从低到高，获取排名
     *
     * @param key    键
     * @param object 对象
     * @return 排名
     */
    public Long getRank(String key, Object object) {
        Long rank = redisTemplate.opsForZSet().rank(key, object);
        if (rank == null) return redisTemplate.opsForZSet().size(key);
        return rank;
    }

    /**
     * 按照分数，从高到低，获取排名
     *
     * @param key    键
     * @param object 对象
     * @return 排名
     */
    public Long getReserveRank(String key, Object object) {
        Long rank = redisTemplate.opsForZSet().reverseRank(key, object);
        if (rank == null) return redisTemplate.opsForZSet().size(key);
        return rank;
    }

    /**
     * 存入一条数据到sorted set    时间作为分数
     *
     * @param key    键
     * @param object 对象
     */
    public boolean storeZSetByTime(String key, Object object) {
        long now = System.currentTimeMillis();
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, object, now));
    }

    /**
     * 存入一条数据到sorted set，自定义分数
     *
     * @param key    键
     * @param object 对象
     * @param score  分数，自己设定的
     * @return 是否成功
     */
    public boolean storeZSetByScore(String key, Object object, double score) {
        return Boolean.TRUE.equals(redisTemplate.opsForZSet().add(key, object, score));
    }

    /**
     * 将ZSetTime集合转换为TypedTuple集合
     * TypedTuple 是 Spring Data Redis 中表示 ZSet 元素及其分数的对象。
     *
     * @param zSetTimes 自定义的类 ZSetTimes集合，可以是set或list等
     * @return TypedTuple集合，TypedTuple类第一个元素是Object，第二个元素是Score
     */
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByTime(Collection<ZSetTime> zSetTimes) {
        return zSetTimes.stream()
                .map(zSetTime -> new DefaultTypedTuple<>(zSetTime.getObject(), (double) zSetTime.getTime().getTime()))
                .collect(Collectors.toSet());
    }

    /**
     * 将ZSetScore集合转换为TypedTuple集合
     * TypedTuple 是 Spring Data Redis 中表示 ZSet 元素及其分数的对象。
     *
     * @param zSetScores 自定义的类 ZSetScores集合，可以是set或list等
     * @return TypedTuple集合，TypedTuple类第一个元素是Object，第二个元素是Score
     */
    private Set<ZSetOperations.TypedTuple<Object>> convertToTupleSetByScore(Collection<ZSetScore> zSetScores) {
        return zSetScores.stream()
                .map(zSetScore -> new DefaultTypedTuple<>(zSetScore.getObject(), zSetScore.getScore()))
                .collect(Collectors.toSet());
    }

    /**
     * 按照时间顺序，批量将ZSetTimes存入redis
     *
     * @param key       键
     * @param ZSetTimes 自定义的类 ZSetTimes，包含对象和时间
     * @return 插入元素的个数
     */
    public Long storeZSetOfCollectionByTime(String key, Collection<ZSetTime> ZSetTimes) {
        return redisTemplate.opsForZSet().add(key, convertToTupleSetByTime(ZSetTimes));
    }

    /**
     * 按照分数排序，批量将ZSetScores存入redis
     *
     * @param key        键
     * @param ZSetScores 自定义类ZSetScore，包含对象和分数
     * @return 存入对象的个数
     */
    public Long storeZSetOfCollectionByScore(String key, Collection<ZSetScore> ZSetScores) {
        return redisTemplate.opsForZSet().add(key, convertToTupleSetByScore(ZSetScores));
    }

    /**
     * 查看匹配数目
     *
     * @param key 键
     * @param min 起始分数
     * @param max 结束分数
     * @return key中符合分数条件的数目
     */
    public Long getZCount(String key, long min, long max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 根据键，获取键对应的整个集合元素个数
     *
     * @param key 键
     * @return 键对应的集合有多少个元素
     */
    public Long getZSetNumber(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 删除Set指定key下的对象
     *
     * @param key   键
     * @param value 对象值
     */
    public void deleteZSetMember(String key, Object value) {
        redisTemplate.opsForZSet().remove(key, value);
    }

    /**
     * 查询某个元素的分数
     *
     * @param key   键
     * @param value 这个键对应的集合中，元素value
     * @return value在集合中的分数
     */
    public Double getZScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 对某个元素增加分数
     *
     * @param key   键
     * @param value 键key对应的集合中的元素
     * @param score 要增加的分数
     * @return 增加分数后，元素value的总分数
     */
    public Double incrementZScore(String key, Object value, double score) {
        return redisTemplate.opsForZSet().incrementScore(key, value, score);
    }

    /**
     * 集合ZSet中是否存在目标对象
     *
     * @param key   键
     * @param value 对象
     * @return 判断对象value是否在键key对应的ZSet中
     */
    public Boolean isExistInZSet(String key, Object value) {
        Double d = getZScore(key, value);
        return null != d;
    }

    /**
     * 添加元素至key对应的ZSet集合中，并且根据limit限制集合的大小。按照时间顺序
     *
     * @param key   键
     * @param value 待添加的元素
     * @param limit 限制集合大小
     * @return 是否添加成功。
     */
    public Boolean zSetByLimit(String key, Object value, Integer limit) {
        Boolean result = this.storeZSetByTime(key, value);
        // 存入数据后，查询ZSet中的数量
        Long count = this.getZSetNumber(key);
        // 如果数量大于limit，则进行清除操作，清除之前的数据
        if (count != null && count > limit) {
            redisTemplate.opsForZSet().removeRange(key, 0, count - limit - 1);
        }
        return result;
    }

    //--------------------有序集合ZSet相关操作结束--------------------

    //--------------------无序集合Set相关操作开始--------------------

    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * set集合获取
     *
     * @param key 键
     * @return key对应的集合
     */
    public Set<Object> getSetMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 集合set中是否存在目标对象
     *
     * @param key   键
     * @param value 对象
     * @return 是否存在对象
     */
    public Boolean isSetMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 向SET中添加无过期时间的对象
     *
     * @param key   键
     * @param value 对象
     */
    public void addSetMember(String key, Object value) {
        redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 删除SET中的数据
     *
     * @param key   键
     * @param value 对象
     */
    public void deleteSetMember(String key, Object value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    /**
     * 获取hashKey对应的所有键值
     *
     * @param key 键
     * @return 对应的多个键值
     */
    public Map<Object, Object> hashGet(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 查询SET大小
     *
     * @param key 键
     * @return 键key对应集合的大小
     */
    public Long getSetSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 随机返回集合中count个元素的集合
     *
     * @param key   键
     * @param count 返回的集合的元素个数
     * @return 随机的元素集合
     */
    public Set<Object> sRandMember(String key, Integer count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    //--------------------无序集合Set相关操作结束--------------------

    //--------------------字符串存储String相关操作开始--------------------

    /**
     * 存储简单数据类型
     * 不用更新的缓存信息
     *
     * @param key   键
     * @param value 值
     */
    public void setValue(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 实体类转换成string再进行操作（JSON）
     * 不用更新的缓存信息
     *
     * @param key   键
     * @param value 对象值
     */
    public void setObjectValue(String key, Object value) {
        String jsonString = JSON.toJSONString(value);
        setValue(key, jsonString);
    }

    /**
     * 使用默认有效期存储实体类，将实体类转换成JSON
     *
     * @param key   键
     * @param value 值
     */
    public void setExObjectValue(String key, Object value) {
        String jsonString = JSON.toJSONString(value);
        setExValue(key, jsonString);
    }

    /**
     * 使用指定有效期存储实体类
     *
     * @param key      键
     * @param value    值
     * @param time     指定的有效期时间
     * @param timeUnit 单位
     */
    public void setExObjectValue(String key, Object value, long time, TimeUnit timeUnit) {
        String jsonString = JSON.toJSONString(value);
        setExValue(key, jsonString, time, timeUnit);
    }

    /**
     * 使用 默认有效期 和 默认时间单位（秒） 存储简单数据类型
     *
     * @param key   键
     * @param value 对象值
     */
    public void setExValue(String key, Object value) {
        setExValue(key, value, REDIS_DEFAULT_EXPIRE_TIME, REDIS_DEFAULT_EXPIRE_TIME_UNIT);
    }

    /**
     * 使用 指定有效期 和 默认时间单位 存储简单数据类型
     *
     * @param key   键
     * @param value 对象值
     * @param time  指定的有效期
     */
    public void setExValue(String key, Object value, long time) {
        setExValue(key, value, time, REDIS_DEFAULT_EXPIRE_TIME_UNIT);
    }

    /**
     * 使用 指定有效期 和 指定时间单位 存储简单数据类型
     *
     * @param key      键
     * @param value    值
     * @param time     指定的有效期时间
     * @param timeUnit 时间单位
     */
    public void setExValue(String key, Object value, long time, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value, time, timeUnit);
    }

    /**
     * 获取简单数据类型
     *
     * @param key 键
     * @return 键对应的值object，单个值，不是集合
     */
    public Object getValue(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取实体类的JSONString
     *
     * @param key 键
     * @return JSON字符串
     */
    public String getObjectString(String key) {
        return (String) redisTemplate.opsForValue().get(key);
    }

    /**
     * 根据传入的类型获取实体类
     */
    public <T> T getObjectByClass(String key, Class<T> clazz) {
        String objectString = (String) redisTemplate.opsForValue().get(key);
        if (StringUtils.isNotBlank(objectString)) {
            return JSONObject.parseObject(objectString, clazz);
        }
        return null;
    }

    /**
     * 获取list中全部数据
     *
     * @param key
     * @param clazz
     * @return
     */
    public <T> List<T> getAllList(String key, Class<T> clazz) {
        List list = this.redisTemplate.opsForList().range(key, 0, -1);
        List<T> resultList = new ArrayList<>();
        for (Object temp : list) {
            resultList.add(JSON.parseObject((String) temp, clazz));
        }
        return resultList;
    }

    /**
     * 删除简单数据类型或实体类
     *
     * @param key
     */
    public void deleteValue(String key) {
        redisTemplate.opsForValue().getOperations().delete(key);
    }

}
