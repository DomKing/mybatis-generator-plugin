/*
 * Copyright (c) 2017.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.itfsw.mybatis.generator.plugins;

import com.itfsw.mybatis.generator.plugins.tools.*;
import org.apache.ibatis.session.SqlSession;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.XMLParserException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * ---------------------------------------------------------------------------
 *
 * ---------------------------------------------------------------------------
 * @author: hewei
 * @time:2017/7/7 16:59
 * ---------------------------------------------------------------------------
 */
public class LogicalDeletePluginTest {
    /**
     * 初始化数据库
     */
    @BeforeClass
    public static void init() throws SQLException, IOException, ClassNotFoundException {
        DBHelper.createDB("scripts/LogicalDeletePlugin/init.sql");
    }

    /**
     * 测试配置异常
     */
    @Test
    public void testWarnings() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        // 1. 不支持的类型
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unsupport-type.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb逻辑删除列(ts_2)的类型不在支持范围（请使用数字列，字符串列，布尔列）！");

        // 2. 没有找到配置的逻辑删除列
        tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unfind-column.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb没有找到您配置的逻辑删除列(ts_999)！");

        // 3. 没有配置逻辑删除值
        tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator-with-unconfig-logicalDeleteValue.xml");
        tool.generate();

        Assert.assertEquals(tool.getWarnings().get(0), "itfsw(逻辑删除插件):tb没有找到您配置的逻辑删除值，请全局或者局部配置logicalDeleteValue和logicalUnDeleteValue值！");
    }

    /**
     * 测试 logicalDeleteByExample
     */
    @Test
    public void testLogicalDeleteByExample() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andIdEqualTo", 1l);

                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "logicalDeleteByExample", tbExample.getObject());
                Assert.assertEquals(sql, "update tb set del_flag = 1 WHERE (  id = '1' )");
                // 验证执行
                Object result = tbMapper.invoke("logicalDeleteByExample", tbExample.getObject());
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select del_flag from tb where id = 1");
                rs.first();
                Assert.assertEquals(rs.getInt("del_flag"), 1);
            }
        });
    }

    /**
     * 测试 logicalDeleteByPrimaryKey
     */
    @Test
    public void testLogicalDeleteByPrimaryKey() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "logicalDeleteByPrimaryKey", 2l);
                Assert.assertEquals(sql, "update tb set del_flag = 1 where id = 2");
                // 验证执行
                Object result = tbMapper.invoke("logicalDeleteByPrimaryKey", 2l);
                Assert.assertEquals(result, 1);
                ResultSet rs = DBHelper.execute(sqlSession.getConnection(), "select del_flag from tb where id = 2");
                rs.first();
                Assert.assertEquals(rs.getInt("del_flag"), 1);
            }
        });
    }

    /**
     * 测试 selectNotDeletedByPrimaryKey
     */
    @Test
    public void testSelectNotDeletedByPrimaryKey() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectNotDeletedByPrimaryKey", 3L);
                Assert.assertEquals(sql, "select id, del_flag, ts_1, ts_3, ts_4 from tb where id = 3 and del_flag <> 1");
                // 验证执行
                Object result = tbMapper.invoke("selectNotDeletedByPrimaryKey", 3L);
                Assert.assertEquals(result, null);
            }
        });
    }

    /**
     * 测试关联生成的方法和常量
     */
    @Test
    public void testOtherMethods() throws IOException, XMLParserException, InvalidConfigurationException, InterruptedException, SQLException {
        MyBatisGeneratorTool tool = MyBatisGeneratorTool.create("scripts/LogicalDeletePlugin/mybatis-generator.xml");
        tool.generate(new AbstractShellCallback() {
            @Override
            public void reloadProject(SqlSession sqlSession, ClassLoader loader, String packagz) throws Exception{
                ObjectUtil tbMapper = new ObjectUtil(sqlSession.getMapper(loader.loadClass(packagz + ".TbMapper")));

                ObjectUtil tbExample = new ObjectUtil(loader, packagz + ".TbExample");
                ObjectUtil criteria = new ObjectUtil(tbExample.invoke("createCriteria"));
                criteria.invoke("andDeleted", true);
                criteria.invoke("andIdEqualTo", 3l);


                // 验证sql
                String sql = SqlHelper.getFormatMapperSql(tbMapper.getObject(), "selectByExample", tbExample.getObject());
                Assert.assertEquals(sql, "select id, del_flag, ts_1, ts_3, ts_4 from tb WHERE (  del_flag = '1' and id = '3' )");
                // 验证执行
                Object result = tbMapper.invoke("selectByExample", tbExample.getObject());
                Assert.assertEquals(((List)result).size(), 1);
            }
        });
    }
}
