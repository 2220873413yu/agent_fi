package com.xms.common.thread;

import lombok.experimental.UtilityClass;

import java.util.Arrays;

/**
 * Runtime 相关工具
 *
 */
@UtilityClass
public class RuntimeKit {
	/**
	 * 默认使用 Runtime.getRuntime().availableProcessors()。
	 * 如果有一些特殊环境需要模拟的，可以设置该变量。
	 */
	public int availableProcessors = Runtime.getRuntime().availableProcessors();

	/**
	 * 数量是不大于 Runtime.getRuntime().availableProcessors() 的 2 次幂。
	 * 当 availableProcessors 的值分别为 4、8、12、16、32 时，对应的数量则是 4、8、8、16、32。
	 * <p>
	 * for example
	 * <table>
	 *     <thead>
	 *         <th scope="col">availableProcessors 值</th>
	 *         <th scope="col">实际值</th>
	 *     </thead>
	 *     <tbody>
	 *         <tr><td>4</td><td>4</td></tr>
	 *         <tr><td>8</td><td>8</td></tr>
	 *         <tr><td>12</td><th scope="row">8</th></tr>
	 *         <tr><td>16</td><td>16</td></tr>
	 *         <tr><td>32</td><td>32</td></tr>
	 *     </tbody>
	 * </table>
	 * <p>
	 * 另外，如果有一些特殊环境需要模拟的，可以设置该变量。
	 */
	public int availableProcessors2n = availableProcessors2n();
	public int availableLatestProcessors2n = availableLatestProcessors2n();

	static int availableProcessors2n() {
		int n = RuntimeKit.availableProcessors;
		n |= (n >> 1);
		n |= (n >> 2);
		n |= (n >> 4);
		n |= (n >> 8);
		n |= (n >> 16);
		return (n + 1) >> 1;
	}

	static int availableLatestProcessors2n() {
		int n = RuntimeKit.availableProcessors;

		// // 如果 n 已经是二次幂，直接返回
		// if ((n & (n - 1)) == 0) {
		// 	return n;
		// }
		if ((n & (n - 1)) == 0) {
			// 如果 n 是 2 的幂，则返回下一个更大的 2 的幂
			return n << 1;
		}
		// 否则，通过位运算计算
		n--;
		n |= n >> 1;
		n |= n >> 2;
		n |= n >> 4;
		n |= n >> 8;
		n |= n >> 16;
		return n + 1;
	}

}
