package com.intelligent.bot.enums.mj;


public enum TaskAction {
	/**
	 * 生成图片.
	 */
	IMAGINE,
	/**
	 * 选中放大.
	 */
	UPSCALE,
	/**
	 * 选中其中的一张图，生成四张相似的.
	 */
	VARIATION,
	/**
	 * 重新执行.
	 */
	REROLL,
	/**
	 * 图转prompt.
	 */
	DESCRIBE,
	/**
	 * 提示词分析.
	 */
	SHORTEN,
	/**
	 * 多图混合.
	 */
	BLEND,

	/**
	 * Vary(Strong).
	 */
	VARY_STRONG,

	/**
	 * Vary(Subtle).
	 */
	VARY_SUBTLE,

	/**
	 * UPSCALE(2x).
	 */
	UPSCALE2,

	/**
	 * UPSCALE(4x).
	 */
	UPSCALE4,

	/**
	 * Zoom Out 2x.
	 */
	ZOOM_OUT_2X,

	/**
	 * Zoom Out 1.5x.
	 */
	ZOOM_OUT_15X,

	/**
	 * ⬅️
	 */
	PAN_LEFT,
	/**
	 * ➡️
	 */
	PAN_RIGHT,
	/**
	 * ⬆️
	 */
	PAN_UP,
	/**
	 * ⬇️
	 */
	PAN_DOWN
}
