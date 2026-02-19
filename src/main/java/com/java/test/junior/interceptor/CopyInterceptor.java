package com.java.test.junior.interceptor;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class CopyInterceptor implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler handler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = handler.getBoundSql();
        String sql = boundSql.getSql();

        if (sql != null && sql.contains("COPY_TRIGGER")) {
            Connection connection = (Connection) invocation.getArgs()[0];

            Object paramObj = boundSql.getParameterObject();
            Reader reader = null;

            if (paramObj instanceof Map) {
                reader = (Reader) ((Map<?, ?>) paramObj).get("reader");
            }

            if (reader != null) {
                executeNativeCopy(connection, reader);
            }

            return connection.prepareStatement("SELECT 1");
        }

        return invocation.proceed();
    }

    private void executeNativeCopy(Connection connection, Reader reader) throws SQLException, IOException {
        BaseConnection pgConn = connection.unwrap(BaseConnection.class);
        CopyManager copyManager = new CopyManager(pgConn);
        copyManager.copyIn("COPY product (name, price, description, user_id) FROM STDIN WITH CSV HEADER", reader);
    }
}