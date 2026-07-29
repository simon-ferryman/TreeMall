"""
generate_tabbar_icons.py — TabBar 图标生成脚本
==============================================

【功能说明】
生成 8 个 TabBar PNG 图标（首页/分类/购物车/用户 × 正常态/选中态），
每个图标 81×81 像素，使用 Pillow 库绘制。

【设计令牌】
- 未选中颜色：#8E8E93（iOS 次要灰）
- 选中颜色：  #007AFF（Apple Blue 主题色）
- 背景：透明

【运行方式】
1. 确保已安装 Pillow：pip install Pillow
2. 运行：python generate_tabbar_icons.py
3. 图标输出到：./static/tabbar/ 目录

【使用场景】
在项目开发初期，TabBar 图标需要占位文件才能编译通过。
后续可替换为设计师提供的正式图标。
"""

import os
from PIL import Image, ImageDraw

# ==================== 配置 ====================

# 图标尺寸（像素）
SIZE = 81

# 颜色定义 (RGBA)
COLOR_ACTIVE = (0, 122, 255, 255)    # #007AFF — 选中态蓝色
COLOR_INACTIVE = (142, 142, 147, 255) # #8E8E93 — 未选中态灰色

# 输出目录
OUTPUT_DIR = os.path.join(os.path.dirname(__file__), 'static', 'tabbar')


def create_icon(name, color, draw_func):
    """
    创建一个图标并保存为 PNG

    Args:
        name: 文件名（不含扩展名）
        color: 绘制颜色 (R, G, B, A)
        draw_func: 绘制函数，接收 (draw, color, size) 参数
    """
    img = Image.new('RGBA', (SIZE, SIZE), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw_func(draw, color, SIZE)
    filepath = os.path.join(OUTPUT_DIR, f'{name}.png')
    img.save(filepath, 'PNG')
    print(f'  ✓ 已生成: {filepath}')


# ==================== 图标绘制函数 ====================

def draw_home(draw, color, size):
    """绘制首页图标（房子形状）"""
    m = 12  # 边距
    cx, cy = size // 2, size // 2
    # 屋顶（三角形）
    draw.polygon([
        (cx, m + 4),
        (m + 4, cy - 4),
        (size - m - 4, cy - 4)
    ], fill=color)
    # 屋身（矩形）
    draw.rectangle([m + 8, cy - 4, size - m - 8, size - m - 4], fill=color)
    # 门
    door_color = (255, 255, 255, 255)
    draw.rectangle([cx - 8, cy + 8, cx + 8, size - m - 4], fill=door_color)


def draw_category(draw, color, size):
    """绘制分类图标（网格形状）"""
    m = 14
    gap = 6
    w = (size - 2 * m - gap) // 2
    # 四个方块
    squares = [
        (m, m, m + w, m + w),
        (m + w + gap, m, m + 2 * w + gap, m + w),
        (m, m + w + gap, m + w, m + 2 * w + gap),
        (m + w + gap, m + w + gap, m + 2 * w + gap, m + 2 * w + gap),
    ]
    for sq in squares:
        draw.rectangle(sq, fill=color)


def draw_cart(draw, color, size):
    """绘制购物车图标（购物车形状）"""
    m = 12
    # 车身
    draw.rectangle([m + 4, m + 20, size - m - 4, size - m], fill=color)
    # 轮子
    wheel_color = (255, 255, 255, 255)
    draw.ellipse([m + 8, size - m - 14, m + 22, size - m], fill=wheel_color)
    draw.ellipse([size - m - 22, size - m - 14, size - m - 8, size - m], fill=wheel_color)
    # 把手
    draw.rectangle([size - m - 8, m + 6, size - m, m + 20], fill=color)


def draw_user(draw, color, size):
    """绘制用户图标（人形）"""
    m = 14
    cx, cy = size // 2, size // 2
    # 头（圆形）
    r = 10
    draw.ellipse([cx - r, m + 2, cx + r, m + 2 + 2 * r], fill=color)
    # 身体（半圆 + 矩形）
    body_top = m + 2 + 2 * r + 2
    draw.arc([cx - 14, body_top, cx + 14, body_top + 28], 0, 180, fill=color, width=2)
    # 简化：用矩形
    draw.rectangle([cx - 12, body_top + 2, cx + 12, size - m - 4], fill=color)


# ==================== 图标名称映射 ====================

ICONS = {
    'home': draw_home,
    'home-active': draw_home,
    'category': draw_category,
    'category-active': draw_category,
    'cart': draw_cart,
    'cart-active': draw_cart,
    'user': draw_user,
    'user-active': draw_user,
}


def main():
    """主函数：生成所有 TabBar 图标"""
    # 创建输出目录
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    print(f'开始生成 {len(ICONS)} 个 TabBar 图标...')
    print(f'输出目录: {OUTPUT_DIR}\n')

    for name, draw_func in ICONS.items():
        # 根据名称判断使用选中态还是未选中态颜色
        color = COLOR_ACTIVE if 'active' in name else COLOR_INACTIVE
        create_icon(name, color, draw_func)

    print(f'\n✅ 全部完成！共生成 {len(ICONS)} 个图标')
    print(f'请将 static/tabbar/ 目录复制到项目根目录下的 static/tabbar/')


if __name__ == '__main__':
    main()