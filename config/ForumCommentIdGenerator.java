package com.example.moodmovies.config;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class ForumCommentIdGenerator implements IdentifierGenerator {
    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Connection connection = null;
        String generatedId = null;
        try {
            connection = session.getJdbcConnectionAccess().obtainConnection();
            try (CallableStatement stmt = connection.prepareCall("{call id_generator(?, ?)}")) {
                stmt.setString(1, "COM"); 
                stmt.registerOutParameter(2, Types.VARCHAR);
                stmt.execute();
                generatedId = stmt.getString(2);
            }
        } catch (SQLException e) {
            throw new HibernateException("ID üretimi sırasında veritabanı hatası oluştu (ForumCommentIdGenerator)", e);
        }
        if (generatedId == null) {
            throw new HibernateException("ID üretimi başarısız, stored procedure null ID döndürdü (ForumCommentIdGenerator)");
        }
        return generatedId;
    }
}