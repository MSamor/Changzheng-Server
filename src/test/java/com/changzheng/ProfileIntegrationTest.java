package com.changzheng;

import com.changzheng.entity.User;
import com.changzheng.entity.StepExchange;
import com.changzheng.mapper.StepExchangeMapper;
import com.changzheng.service.StepService;
import java.math.BigDecimal;
import java.time.LocalDate;
import com.changzheng.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
class ProfileIntegrationTest {
    private static final Path TEMP;
    static {
        try { TEMP = Files.createTempDirectory("changzheng-profile-test-"); }
        catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", () -> "jdbc:sqlite:" + TEMP.resolve("test.db"));
        registry.add("app.upload.directory", () -> TEMP.resolve("uploads").toString());
        registry.add("app.upload.public-base-url", () -> "https://example.test");
        registry.add("wechat.mock", () -> "true");
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserMapper users;
    @Autowired StepService steps;
    @Autowired com.changzheng.service.WechatService wechat;
    @Autowired StepExchangeMapper exchanges;
    private User user;

    @BeforeEach
    void createUser() {
        user = new User();
        user.setOpenid(UUID.randomUUID().toString());
        user.setNickname("原昵称");
        user.setCity("遵义");
        user.setProvince("贵州");
        user.setGender(1);
        user.setStatus(1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        users.insert(user);
    }

    @Test
    void newLoginPersistsDefaultNicknameAndPreservesCustomProfile() {
        String code = UUID.randomUUID().toString();
        User created = wechat.login(code);
        assertThat(users.selectById(created.getId()).getNickname()).isEqualTo("长征参与者");
        assertThat(created.getAvatarUrl()).isNullOrEmpty();
        created.setNickname("自己的昵称");
        created.setAvatarUrl("https://example.test/custom.png");
        users.updateById(created);
        User again = wechat.login(code);
        assertThat(again.getNickname()).isEqualTo("自己的昵称");
        assertThat(again.getAvatarUrl()).isEqualTo("https://example.test/custom.png");
    }

    @Test
    void blankLegacyNicknameUsesDefaultInProfileAndRank() throws Exception {
        user.setNickname(" ");
        users.updateById(user);
        mvc.perform(get("/home/userInfo/" + user.getId()))
                .andExpect(jsonPath("$.data.nickname").value("长征参与者"));
        mvc.perform(get("/rank/getAllRank").param("userId", user.getId().toString()))
                .andExpect(jsonPath("$.data.allRank[?(@.userId == " + user.getId() + ")].nickname")
                        .value(org.hamcrest.Matchers.hasItem("长征参与者")));
    }

    @Test
    void avatarUploadSaveReadAndRankUsePersistedProfile() throws Exception {
        var out = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(80, 40, BufferedImage.TYPE_INT_RGB), "png", out);
        var file = new MockMultipartFile("file", "../../avatar.png", "image/png", out.toByteArray());
        String response = mvc.perform(multipart("/home/avatar").file(file).param("userId", user.getId().toString()))
                .andExpect(jsonPath("$.code").value(0)).andReturn().getResponse().getContentAsString();
        String avatar = json.readTree(response).path("data").path("avatarUrl").asText();
        assertThat(avatar).startsWith("https://example.test/uploads/avatars/").endsWith(".png");
        String path = avatar.substring("https://example.test".length());
        mvc.perform(get(path)).andExpect(status().isOk()).andExpect(content().contentType("image/png"));
        mvc.perform(post("/home/saveUserInfo").contentType("application/json").content(json.writeValueAsString(
                Map.of("userId", user.getId(), "nickname", " 新昵称 ", "avatarUrl", avatar))))
                .andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.openid").doesNotExist());
        User stored = users.selectById(user.getId());
        assertThat(stored.getAvatarUrl()).isEqualTo(avatar);
        assertThat(stored.getCity()).isEqualTo("遵义");
        assertThat(stored.getProvince()).isEqualTo("贵州");
        assertThat(stored.getGender()).isEqualTo(1);
        mvc.perform(get("/home/userInfo/" + user.getId()))
                .andExpect(jsonPath("$.data.nickname").value("新昵称"))
                .andExpect(jsonPath("$.data.avatarUrl").value(avatar))
                .andExpect(jsonPath("$.data.openid").doesNotExist());
        for (String rank : java.util.List.of("getAllRank", "getTodayRank")) {
            String list = rank.equals("getAllRank") ? "allRank" : "todayRank";
            mvc.perform(get("/rank/" + rank).param("userId", user.getId().toString()))
                    .andExpect(jsonPath("$.data." + list + "[?(@.userId == " + user.getId() + ")].nickname")
                            .value(org.hamcrest.Matchers.hasItem("新昵称")))
                    .andExpect(jsonPath("$.data." + list + "[?(@.userId == " + user.getId() + ")].avatarUrl")
                            .value(org.hamcrest.Matchers.hasItem(avatar)));
        }
    }

    @Test
    void rejectsTemporaryPathsAndEmptyNicknameWithoutChangingDatabase() throws Exception {
        for (Map<String, Object> payload : java.util.List.of(
                Map.<String, Object>of("userId", user.getId(), "nickname", "  "),
                Map.<String, Object>of("userId", user.getId(), "avatarUrl", "wxfile://tmp"),
                Map.<String, Object>of("userId", user.getId(), "nickname", "长".repeat(33)))) {
            mvc.perform(post("/home/saveUserInfo").contentType("application/json").content(json.writeValueAsString(payload)))
                    .andExpect(jsonPath("$.code").value(40001));
        }
        assertThat(users.selectById(user.getId()).getNickname()).isEqualTo("原昵称");
    }

    @Test
    void rejectsFakeImagesAndOversizedFiles() throws Exception {
        for (MockMultipartFile file : java.util.List.of(
                new MockMultipartFile("file", "avatar.png", "image/png", "not a picture".getBytes()),
                new MockMultipartFile("file", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1]))) {
            mvc.perform(multipart("/home/avatar").file(file).param("userId", user.getId().toString()))
                    .andExpect(jsonPath("$.code").value(40001));
        }
    }

    @Test
    void rejectsUnknownUserBeforeWritingAvatar() throws Exception {
        mvc.perform(multipart("/home/avatar").file(new MockMultipartFile("file", "x.png", "image/png", new byte[]{1}))
                .param("userId", "9223372036854775807")).andExpect(jsonPath("$.code").value(40002));
    }

    @Test
    void fiveHundredStepsRedeemsFractionalMileageAndCannotBeRedeemedTwice() throws Exception {
        steps.saveMax(user.getId(), 499);
        mvc.perform(put("/map/exchangeSteps/" + user.getId())).andExpect(jsonPath("$.code").value(40005));
        steps.saveMax(user.getId(), 500);
        mvc.perform(put("/map/exchangeSteps/" + user.getId()))
                .andExpect(jsonPath("$.code").value(0)).andExpect(jsonPath("$.data.mileage").value(0.6));
        steps.saveMax(user.getId(), 5000);
        mvc.perform(put("/map/exchangeSteps/" + user.getId()))
                .andExpect(jsonPath("$.data.mileage").value(0.6));
        mvc.perform(get("/map/getAllData/" + user.getId()))
                .andExpect(jsonPath("$.data.totalMileage").value(0.6))
                .andExpect(jsonPath("$.data.nextStopKilometer").value(19.4))
                .andExpect(jsonPath("$.data.stepUnit").value(500))
                .andExpect(jsonPath("$.data.mileageUnit").value(0.6))
                .andExpect(jsonPath("$.data.minimumExchangeSteps").value(500))
                .andExpect(jsonPath("$.data.convertSteps").value(0));
        mvc.perform(get("/rank/getAllRank").param("userId", user.getId().toString()))
                .andExpect(jsonPath("$.data.allRank[?(@.userId == " + user.getId() + ")].totalMileage")
                        .value(org.hamcrest.Matchers.hasItem(0.6)));
    }

    @Test
    void thirtyDaysOfFiveThousandStepsReachGansuWithExactTotal() throws Exception {
        for (int day = 1; day <= 29; day++) {
            StepExchange exchange = new StepExchange();
            exchange.setUserId(user.getId());
            exchange.setExchangeDate(LocalDate.now().minusDays(day));
            exchange.setStepCount(5000);
            exchange.setMileage(new BigDecimal("6"));
            exchange.setStepUnit(500);
            exchange.setMileageUnit(new BigDecimal("0.6"));
            exchange.setStatus(1);
            exchange.setCreatedAt(LocalDateTime.now());
            exchange.setUpdatedAt(LocalDateTime.now());
            exchanges.insert(exchange);
        }
        mvc.perform(get("/map/getAllData/" + user.getId()))
                .andExpect(jsonPath("$.data.totalMileage").value(174))
                .andExpect(jsonPath("$.data.nextStopKilometer").value(6))
                .andExpect(jsonPath("$.data.routeCompleted").value(false));
        steps.saveMax(user.getId(), 5000);
        String response = mvc.perform(put("/map/exchangeSteps/" + user.getId()))
                .andExpect(jsonPath("$.code").value(0)).andReturn().getResponse().getContentAsString();
        assertThat(json.readTree(response).path("data").path("mileage").decimalValue())
                .isEqualByComparingTo("6");
        mvc.perform(get("/map/getAllData/" + user.getId()))
                .andExpect(jsonPath("$.data.totalMileage").value(180))
                .andExpect(jsonPath("$.data.nextStopKilometer").value(0))
                .andExpect(jsonPath("$.data.arrivedCityName").value("甘肃"))
                .andExpect(jsonPath("$.data.routeCompleted").value(true));
    }

    @Test
    void partialUnitsRoundDownAndDecimalAccumulationStaysExact() throws Exception {
        steps.saveMax(user.getId(), 999);
        mvc.perform(put("/map/exchangeSteps/" + user.getId()))
                .andExpect(jsonPath("$.data.mileage").value(0.6));
        for (int day = 1; day <= 2; day++) {
            StepExchange exchange = new StepExchange();
            exchange.setUserId(user.getId());
            exchange.setExchangeDate(LocalDate.now().minusDays(day));
            exchange.setStepCount(500);
            exchange.setMileage(new BigDecimal("0.6"));
            exchange.setStepUnit(500);
            exchange.setMileageUnit(new BigDecimal("0.6"));
            exchange.setStatus(1);
            exchange.setCreatedAt(LocalDateTime.now());
            exchange.setUpdatedAt(LocalDateTime.now());
            exchanges.insert(exchange);
        }
        mvc.perform(get("/map/getAllData/" + user.getId()))
                .andExpect(jsonPath("$.data.totalMileage").value(1.8))
                .andExpect(jsonPath("$.data.nextStopKilometer").value(18.2));
    }
}
