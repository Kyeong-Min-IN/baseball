package com.baseball.game.util;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringListTypeHandler extends BaseTypeHandler<List<String>> {

    private static final String DELIMITER = ",";

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, List<String> parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter.stream().collect(Collectors.joining(DELIMITER)));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String columnValue = rs.getString(columnName);
        return columnValue == null ? null : Arrays.asList(columnValue.split(DELIMITER));
    }

    @Override
    public List<String> getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String columnValue = rs.getString(columnIndex);
        return columnValue == null ? null : Arrays.asList(columnValue.split(DELIMITER));
    }

    @Override
    public List<String> getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String columnValue = cs.getString(columnIndex);
        return columnValue == null ? null : Arrays.asList(columnValue.split(DELIMITER));
    }
}
