package com.lixh.login.data.handler;

import com.pgvector.PGvector;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PGvectorTypeHandler extends BaseTypeHandler<PGvector> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, PGvector parameter, JdbcType jdbcType) throws SQLException {
        ps.setObject(i, parameter);
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, String columnName) throws SQLException {
        final Object pgObject = rs.getObject(columnName);
        if (pgObject == null) {
            return null;
        }
        return new PGvector(pgObject.toString());
    }

    @Override
    public PGvector getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        final Object pgObject = rs.getObject(columnIndex);
        if (pgObject == null) {
            return null;
        }
        return new PGvector(pgObject.toString());
    }

    @Override
    public PGvector getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        final Object pgObject = cs.getObject(columnIndex);
        if (pgObject == null) {
            return null;
        }
        return new PGvector(pgObject.toString());
    }
}
