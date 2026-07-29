package com.treemall.mapper;                                            // 声明包路径：在 mapper 包下
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;                 // MyBatis-Plus 提供的通用 Mapper 接口，自带 CRUD
import com.treemall.entity.Cart;                                         // 导入对应的实体类：Cart 实体
import org.apache.ibatis.annotations.Mapper;                            // 标记这是一个 Mapper 接口，Spring 扫描时会注册

/**
 * 购物车 Mapper 接口
 *
 * 协作：Service 调用 CartMapper → CartMapper 操作数据库 → 返回 List<Cart>
 * BaseMapper 已经提供了 insert/update/delete/selectList 等方法，够用了
 * 不需要写 XML，除非复杂查询才需要自定义
 */
@Mapper                                                               // Spring 会自动扫描这个接口并创建实现类
public interface CartMapper extends BaseMapper<Cart> {                    // 继承 BaseMapper<Cart>，泛型指定实体类型
    // BaseMapper 已经包含所有常用方法，这里不需要额外写代码
    // 在 CartMapper 接口中添加
    //绕过逻辑删除过滤，解决同一商品不能二次加入购物车。
    @Select("SELECT * FROM t_cart WHERE user_id = #{userId} AND product_id = #{productId} LIMIT 1")
    Cart selectByUserIdAndProductIdIgnoreLogic(@Param("userId") Long userId, @Param("productId") Long productId);

    @Update("UPDATE t_cart SET quantity = quantity + 1, deleted = 0, checked = 1, updated_at = NOW() WHERE user_id = #{userId} AND product_id = #{productId}")
    int restoreAndIncrementQuantity(@Param("userId") Long userId, @Param("productId") Long productId);
}
