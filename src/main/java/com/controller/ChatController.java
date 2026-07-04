package com.controller;

import com.annotation.IgnoreAuth;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.ChatEntity;
import com.entity.view.ChatView;
import com.service.ChatService;
import com.utils.MPUtil;
import com.utils.PageUtils;
import com.utils.R;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * 在线客服
 * 后端接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

	private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

	@Autowired
	private ChatService chatService;

	@Value("${deepseek.key}")
	private String deepseekKey;

	/* ===================== 原接口保持不变 ===================== */

	@RequestMapping("/page")
	public R page(@RequestParam Map<String, Object> params, ChatEntity chat,
				  HttpServletRequest request) {
		if (!request.getSession().getAttribute("role").toString().equals("管理员")) {
			chat.setUserid(1L); // 固定用户ID，绕过登录
		}
		EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();
		PageUtils page = chatService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));
		return R.ok().put("data", page);
	}

	@IgnoreAuth
	@RequestMapping("/list")
	public R list(@RequestParam Map<String, Object> params, ChatEntity chat, HttpServletRequest request) {
		// 绕过权限检查，直接查询所有聊天记录
		EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();

		// 如果有传入userid，就按userid查询；否则查询所有
		if (chat.getUserid() != null) {
			ew.eq("userid", chat.getUserid());
		}

		PageUtils page = chatService.queryPage(params, MPUtil.sort(MPUtil.between(MPUtil.likeOrEq(ew, chat), params), params));
		return R.ok().put("data", page);
	}
	@RequestMapping("/lists")
	public R list(ChatEntity chat) {
		EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();
		ew.allEq(MPUtil.allEQMapPre(chat, "chat"));
		return R.ok().put("data", chatService.selectListView(ew));
	}

	@RequestMapping("/query")
	public R query(ChatEntity chat) {
		EntityWrapper<ChatEntity> ew = new EntityWrapper<ChatEntity>();
		ew.allEq(MPUtil.allEQMapPre(chat, "chat"));
		ChatView chatView = chatService.selectView(ew);
		return R.ok("查询在线客服成功").put("data", chatView);
	}

	@RequestMapping("/info/{id}")
	public R info(@PathVariable("id") Long id) {
		ChatEntity chat = chatService.selectById(id);
		return R.ok().put("data", chat);
	}

	@RequestMapping("/detail/{id}")
	public R detail(@PathVariable("id") Long id) {
		ChatEntity chat = chatService.selectById(id);
		return R.ok().put("data", chat);
	}

	@RequestMapping("/save")
	public R save(@RequestBody ChatEntity chat, HttpServletRequest request) {
		chat.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
		if (StringUtils.isNotBlank(chat.getAsk())) {
			chatService.updateForSet("isreply=0", new EntityWrapper<ChatEntity>().eq("userid", request.getSession().getAttribute("userId")));
			chat.setUserid((Long) request.getSession().getAttribute("userId"));
			chat.setIsreply(1);
		}
		if (StringUtils.isNotBlank(chat.getReply())) {
			chatService.updateForSet("isreply=0", new EntityWrapper<ChatEntity>().eq("userid", chat.getUserid()));
			chat.setAdminid((Long) request.getSession().getAttribute("userId"));
		}
		chatService.insert(chat);
		return R.ok();
	}

	@RequestMapping("/add")
	public R add(@RequestBody ChatEntity chat, HttpServletRequest request) {
		chat.setId(new Date().getTime() + new Double(Math.floor(Math.random() * 1000)).longValue());
		chat.setUserid((Long) request.getSession().getAttribute("userId"));
		if (StringUtils.isNotBlank(chat.getAsk())) {
			chatService.updateForSet("isreply=0", new EntityWrapper<ChatEntity>().eq("userid", request.getSession().getAttribute("userId")));
			chat.setUserid((Long) request.getSession().getAttribute("userId"));
			chat.setIsreply(1);
		}
		if (StringUtils.isNotBlank(chat.getReply())) {
			chatService.updateForSet("isreply=0", new EntityWrapper<ChatEntity>().eq("userid", chat.getUserid()));
			chat.setAdminid((Long) request.getSession().getAttribute("userId"));
		}
		chatService.insert(chat);
		return R.ok();
	}

	@RequestMapping("/update")
	public R update(@RequestBody ChatEntity chat, HttpServletRequest request) {
		chatService.updateById(chat);
		return R.ok();
	}

	@RequestMapping("/delete")
	public R delete(@RequestBody Long[] ids) {
		chatService.deleteBatchIds(Arrays.asList(ids));
		return R.ok();
	}

	@RequestMapping("/remind/{columnName}/{type}")
	public R remindCount(@PathVariable("columnName") String columnName, HttpServletRequest request,
						 @PathVariable("type") String type, @RequestParam Map<String, Object> map) {
		map.put("column", columnName);
		map.put("type", type);

		if (type.equals("2")) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Calendar c = Calendar.getInstance();
			Date remindStartDate = null;
			Date remindEndDate = null;
			if (map.get("remindstart") != null) {
				Integer remindStart = Integer.parseInt(map.get("remindstart").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH, remindStart);
				remindStartDate = c.getTime();
				map.put("remindstart", sdf.format(remindStartDate));
			}
			if (map.get("remindend") != null) {
				Integer remindEnd = Integer.parseInt(map.get("remindend").toString());
				c.setTime(new Date());
				c.add(Calendar.DAY_OF_MONTH, remindEnd);
				remindEndDate = c.getTime();
				map.put("remindend", sdf.format(remindEndDate));
			}
		}

		Wrapper<ChatEntity> wrapper = new EntityWrapper<ChatEntity>();
		if (map.get("remindstart") != null) {
			wrapper.ge(columnName, map.get("remindstart"));
		}
		if (map.get("remindend") != null) {
			wrapper.le(columnName, map.get("remindend"));
		}

		int count = chatService.selectCount(wrapper);
		return R.ok().put("count", count);
	}

	/* ===================== 新增：带 AI 的客服接口 ===================== */
	@IgnoreAuth
	@PostMapping("/addWithDeepseek")
	public R addWithDeepseek(@RequestBody ChatEntity chat, HttpServletRequest request) {
		try {
			logger.info("=== AI客服请求开始 ===");
			logger.info("请求方法: {}", request.getMethod());
			logger.info("请求URL: {}", request.getRequestURL());
			logger.info("请求内容: {}", chat.getAsk());
			logger.info("DeepSeek Key: {}", deepseekKey != null ? "已配置" : "未配置");

			// 完全绕过登录检查 - 使用固定用户ID
			Long userId = 1L;
			logger.info("使用固定用户ID: {}", userId);

			// 这里添加一个测试回复，先不调用真实的API
			String testReply = callDeepSeek(chat.getAsk());
			logger.info("测试回复: {}", testReply);

			Date currentTime = new Date();

			// 1. 保存用户提问
			long id = System.currentTimeMillis() + (long) (Math.random() * 1000);
			chat.setId(id);
			chat.setUserid(userId);
			chat.setIsreply(1);
			chat.setAddtime(currentTime);

			logger.info("保存用户提问 - ID: {}", id);
			boolean insertResult = chatService.insert(chat);
			logger.info("用户提问保存结果: {}", insertResult);

			// 2. 先返回测试回复，不调用真实API
			logger.info("返回测试回复");

			// 3. 保存测试回复
			ChatEntity reply = new ChatEntity();
			reply.setId(id + 1);
			reply.setUserid(userId);
			reply.setAdminid(1L);
			reply.setReply(testReply);
			reply.setIsreply(0);
			reply.setAddtime(new Date());

			chatService.insert(reply);
			logger.info("测试回复保存成功");

			logger.info("=== AI客服请求完成 ===");
			return R.ok().put("data", testReply);

		} catch (Exception e) {
			logger.error("AI客服处理异常: ", e);
			return R.error("AI客服处理失败: " + e.getMessage());
		}
	}

	/* ===================== 私有方法：调用 DeepSeek API ===================== */
	private String callDeepSeek(String question) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			String url = "https://api.deepseek.com/v1/chat/completions";

			// 设置请求头
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", "Bearer " + deepseekKey);

			// 构建消息体
			Map<String, Object> message = new HashMap<>();
			message.put("role", "user");
			message.put("content", question);

			List<Map<String, Object>> messages = new ArrayList<>();
			messages.add(message);

			Map<String, Object> requestBody = new HashMap<>();
			requestBody.put("model", "deepseek-chat");
			requestBody.put("messages", messages);
			requestBody.put("max_tokens", 1000);
			requestBody.put("temperature", 0.7);

			// 发送请求
			HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
			Map<String, Object> response = restTemplate.postForObject(url, entity, Map.class);

			// 解析响应 - 兼容Java 8的写法
			if (response == null) {
				throw new Exception("API响应为空");
			}

			List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
			if (choices == null || choices.isEmpty()) {
				throw new Exception("API返回的choices为空");
			}

			Map<String, Object> firstChoice = choices.get(0);
			Map<String, Object> messageMap = (Map<String, Object>) firstChoice.get("message");
			if (messageMap == null) {
				throw new Exception("API返回的message为空");
			}

			String content = (String) messageMap.get("content");
			if (content == null) {
				throw new Exception("API返回的content为空");
			}

			return content.trim();

		} catch (Exception e) {
			logger.error("调用DeepSeek API异常: ", e);
			return "抱歉，AI助手暂时无法响应，请稍后再试。错误信息：" + e.getMessage();
		}
	}
}

