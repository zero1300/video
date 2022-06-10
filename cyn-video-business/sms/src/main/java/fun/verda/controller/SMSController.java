package fun.verda.controller;

import fun.verda.vo.MsgVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Slf4j
@RestController
public class SMSController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("captchas")
    public void captcha(@RequestBody MsgVO msgVO) {
        String phone = msgVO.getPhone();
        log.info("要发送的手机号码为: {}", phone);

        // 用于在 redis 中查询的key
        String timeoutKey = "timeout_" + phone;
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(timeoutKey))) {
            throw new RuntimeException("提示: 不允许重发发送");
        }

        try {
            // 随机生成4位验证码
            String code = RandomStringUtils.randomNumeric(4);
            log.info("发送验证码: {}", code);

            // 将验证码存入数据库
            String phoneKey = "phone_" + phone;
            stringRedisTemplate.opsForValue().set(phoneKey, code, 10, TimeUnit.MINUTES);

            // 60 秒内只能发送一次验证码
            stringRedisTemplate.opsForValue().set(timeoutKey, "true", 60, TimeUnit.SECONDS);
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("提示: 短信发送失败");
        }

    }

}
