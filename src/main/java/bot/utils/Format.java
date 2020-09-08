package bot.utils;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;

public class Format {

	public static String bold(String msg) {
		return "**" + msg + "**";
	}

	public static String italic(String msg) {
		return "*" + msg + "*";
	}

	public static String underline(String msg) {
		return "__" + msg + "__";
	}

	public static String ping(String msg) {
		return "<@" + msg + ">";
	}

	public static String decimal(float num) {
		return new DecimalFormat("0.##").format(num);
	}

	public static String fixedLength(String str, int length) {
		return StringUtils.rightPad(str, length);
	}

	public static String code(String msg) {
		return "```" + msg + "```";
	}

	public static String greenCode(String msg) {
		return "```yaml\n" + msg + "```";
	}

	public static String redCode(String msg) {
		return "```diff\n- " + msg + "```";
	}

	public static String blueCode(String msg) {
		return "```CSS\n" + msg + "\n```";
	}
}
