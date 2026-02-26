package com.java.test.junior.interceptor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.Reader;
import java.sql.Connection;
import java.util.Map;

@Intercepts(@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}))
public class CopyInterceptor implements Interceptor {

    private static final String COPY_TRIGGER = "COPY_TRIGGER";
    private static final String RETURN_SQL = "SELECT 1";
    private static final String COPY_SQL = "COPY product (name, price, description, user_id) FROM STDIN WITH CSV HEADER";
    private static final int ARGUMENT_INDEX = 0;
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        BoundSql boundSql = returnBoundSql(invocation);
        String sql = boundSql.getSql();
        if (sql == null || !sql.contains(COPY_TRIGGER)) {
            return invocation.proceed();
        }
        Connection connection = executeInvocation(invocation, boundSql);

        return connection.prepareStatement(RETURN_SQL);
    }
    private BoundSql returnBoundSql(Invocation invocation) {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        return handler.getBoundSql();
    }

    private Connection executeInvocation(Invocation invocation, BoundSql boundSql) throws Exception {
        Connection connection = extractConnection(invocation);
        Reader reader = extractReader(boundSql);

        if (reader == null) {
            return connection;
        }
        executeCopy(connection, reader);
        return connection;
    }

    private Connection extractConnection(Invocation invocation) {
        return (Connection) invocation.getArgs()[ARGUMENT_INDEX];
    }

    private Reader extractReader(BoundSql boundSql) {
        Object params = boundSql.getParameterObject();
        if (params instanceof Map<?, ?> map) {
            return (Reader) map.getOrDefault("reader", null);
        }
        return null;
    }

    private void executeCopy(Connection connection, Reader reader) throws Exception {
        BaseConnection pgConnection = connection.unwrap(BaseConnection.class);
        CopyManager copyManager = new CopyManager(pgConnection);
        copyManager.copyIn(COPY_SQL, reader);
    }
}