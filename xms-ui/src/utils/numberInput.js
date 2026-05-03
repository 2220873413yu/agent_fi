/**
 * 通用数字输入清洗方法
 * @param {string|number} input 原始输入值
 * @param {Object} options 配置项
 * @param {boolean} [options.allowDecimal=true] 是否允许小数
 * @param {number} [options.decimals=2] 保留的小数位，-1 表示不限制
 * @returns {string} 清洗后的字符串
 */
export function sanitizeNumberInput(input, options = {}) {
  const { allowDecimal = true, decimals = 2 } = options;
  let value = `${input ?? ''}`;

  // 过滤非法字符
  value = value.replace(/[^\d.]/g, '');

  if (!allowDecimal) {
    return value.replace(/\./g, '');
  }

  // 去除开头的小数点
  value = value.replace(/^\./, '');

  // 只保留第一个小数点
  value = value
    .replace(/\.{2,}/g, '.')
    .replace('.', '#')
    .replace(/\./g, '')
    .replace('#', '.');

  if (decimals >= 0 && value.includes('.')) {
    const [intPart, decimalPart = ''] = value.split('.');
    value = `${intPart}.${decimalPart.slice(0, decimals)}`;
  }

  return value;
}



