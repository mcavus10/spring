package com.example.moodmovies.config;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * Veritabanı stored procedure'üne dayalı özel yanıt ID üreteci.
 * id_generator adlı stored procedure'ü çağırır ve RSP prefixi ile ID üretir.
 */
public class ResponseIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Connection connection = null;
        String generatedId = null;
        try {
            connection = session.getJdbcConnectionAccess().obtainConnection();
            // Stored procedure çağrısı hazırlığı
            try (CallableStatement stmt = connection.prepareCall("{call id_generator(?, ?)}")) {
                // Prefix parametresi (Bu örnekte "RSP" kullanıyoruz)
                stmt.setString(1, "RSP");
                // Çıkış parametresi (ID için)
                stmt.registerOutParameter(2, Types.VARCHAR);
                
                // Stored procedure'ü çalıştır
                stmt.execute();
                
                // Üretilen ID'yi al
                generatedId = stmt.getString(2);
            }
        } catch (SQLException e) {
            // Loglama eklenebilir
            throw new HibernateException("ID üretimi sırasında veritabanı hatası oluştu (ResponseIdGenerator)", e);
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
            throw new HibernateException("ID üretimi başarısız oldu, stored procedure null ID döndürdü (ResponseIdGenerator)");
        }
        return generatedId;
    }
}
