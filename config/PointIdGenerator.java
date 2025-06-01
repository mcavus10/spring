package com.example.moodmovies.config; // Paket adını kendi projenize göre ayarlayın

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

public class PointIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Connection connection = null;
        String generatedId = null;
        try {
            connection = session.getJdbcConnectionAccess().obtainConnection();
            try (CallableStatement stmt = connection.prepareCall("{call id_generator(?, ?)}")) {
                stmt.setString(1, "POI"); // Prefix for Point ID
                stmt.registerOutParameter(2, Types.VARCHAR);
                stmt.execute();
                generatedId = stmt.getString(2);
            }
        } catch (SQLException e) {
            // Loglama eklenebilir
            throw new HibernateException("ID üretimi sırasında veritabanı hatası oluştu (PointIdGenerator)", e);
        } finally {
            if (connection != null) {
                try {
                    session.getJdbcConnectionAccess().releaseConnection(connection);
                } catch (SQLException ignored) {
                    // Bağlantıyı kapatırken oluşan hatalar görmezden gelinebilir
                }
            }
        }
        if (generatedId == null) {
            throw new HibernateException("ID üretimi başarısız oldu, stored procedure null ID döndürdü (PointIdGenerator)");
        }
        return generatedId;
    }
}