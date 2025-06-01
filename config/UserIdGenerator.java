package com.example.moodmovies.config;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

/**
 * Veritabanı stored procedure'üne dayalı özel kullanıcı ID üreteci.
 * id_generator adlı stored procedure'ü çağırır ve USR prefixi ile ID üretir.
 */
public class UserIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        Connection connection = null;
        try {
            connection = session.getJdbcConnectionAccess().obtainConnection();
            // Stored procedure çağrısı hazırlığı
            try (CallableStatement stmt = connection.prepareCall("{call id_generator(?, ?)}")) {
                // Prefix parametresi
                stmt.setString(1, "USR");
                // Çıkış parametresi (ID için)
                stmt.registerOutParameter(2, java.sql.Types.VARCHAR);
                
                // Stored procedure'ü çalıştır
                stmt.execute();
                
                // Üretilen ID'yi al
                String generatedId = stmt.getString(2);
                
                return generatedId;
            }
        } catch (SQLException e) {
            throw new HibernateException("ID üretimi sırasında hata oluştu", e);
        } finally {
            if (connection != null) {
                try {
                    session.getJdbcConnectionAccess().releaseConnection(connection);
                } catch (SQLException ignored) {
                    // Bağlantıyı kapatırken oluşan hatalar görmezden gelinebilir
                }
            }
        }
    }
}
