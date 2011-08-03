/*
 * Copyright 2004 - 2011 Brian McCallister
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.skife.jdbi.v2.sqlobject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.TestSeparateMapper.Foo.FooMapper;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;


public class TestSeparateMapper
{
    private Handle handle;

    private DBI dbi;

    @Before
    public void setUp() throws Exception
    {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        dbi = new DBI(ds);
        handle = dbi.open();

        handle.execute("create table something (id int primary key, name varchar(100))");
    }

    @After
    public void tearDown() throws Exception
    {
        handle.execute("drop table something");
        handle.close();
    }

    @Test
    public void testSimple() throws Exception
    {
        FooDao fooDao = dbi.onDemand(FooDao.class);

        List<Foo> foos = fooDao.select();
        Assert.assertNotNull(foos);
        Assert.assertEquals(0, foos.size());

        fooDao.insert(1, "John Doe");
        fooDao.insert(2, "Jane Doe");
        List<Foo> foos2 = fooDao.select();
        Assert.assertNotNull(foos2);
        Assert.assertEquals(2, foos2.size());

    }

    public static interface FooDao
    {
        @SqlQuery("select * from something")
        List<Foo> select();

        @SqlUpdate("insert into something (id, name) VALUES (:id, :name)")
        void insert(@Bind("id") int id, @Bind("name") String name);
    }

    @RegisterMapper(FooMapper.class)
    public static class Foo
    {
        private final int id;
        private final String name;

        Foo(final int id, final String name)
        {
            this.id = id;
            this.name = name;
        }

        public int getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public static class FooMapper implements ResultSetMapper<Foo>
        {
            public Foo map(final int index, final ResultSet r, final StatementContext ctx) throws SQLException
            {
                return new Foo(r.getInt("id"), r.getString("name"));
            }
        }
    }
}