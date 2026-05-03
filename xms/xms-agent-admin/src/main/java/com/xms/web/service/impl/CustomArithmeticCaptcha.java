package com.xms.web.service.impl;



import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;

/**
 * @author: GT63S
 * @createDate: 2024/8/13
 */

@Slf4j
public class CustomArithmeticCaptcha extends ArithmeticCaptcha {
	private final static ScriptEngineManager manager = new ScriptEngineManager();
	static {
		//添加这一行
		manager.registerEngineName("customScriptEngineFactory",new NashornScriptEngineFactory());
	}
	public CustomArithmeticCaptcha() {
		super();
	}

	public CustomArithmeticCaptcha(int width, int height) {
		super(width, height);
	}

	public CustomArithmeticCaptcha(int width, int height, int len) {
		super(width, height, len);
	}

	public CustomArithmeticCaptcha(int width, int height, int len, Font font) {
		super(width, height, len, font);
	}
	@Override
	protected char[] alphas() {
		StringBuilder sb = new StringBuilder();
		//定义获取减速和被减速变量
		int num1 = 0;
		int num2 = 0;
		//是否随机到减数标志位，默认false
		boolean flag = Boolean.FALSE;
		for (int i = 0; i < len; i++) {
			int num = num(10);
			sb.append(num);
			//进行赋值
			if (i == 0){
				num1 = num;
			}else {
				num2 = num;
			}

			if (i < len - 1) {
				int type = num(1, 4);
				if (type == 1) {
					sb.append("+");
				} else if (type == 2) {
					sb.append("-");
					//当是减数时打上标记，进行后续处理
					flag = true;
				} else if (type == 3) {
					sb.append("x");
				}
			}

		}

		if (flag) {
			//不能出现差是小于0的结果，如果出现就直接变成+
			if (num1 < num2) {
				sb.replace(1,2,"+");
			}
		}

		ScriptEngine engine = manager.getEngineByName("javascript");
		try {
			chars = String.valueOf(engine.eval(sb.toString().replaceAll("x", "*")));
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		sb.append("=?");
		setArithmeticString(sb.toString());
		return chars.toCharArray();
	}
}
