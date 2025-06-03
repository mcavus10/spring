package com.example.moodmovies.config;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

@Slf4j
public class ListIdGenerator implements IdentifierGenerator {

    @Override
    public Object generate(SharedSessionContractImplementor session, Object object) throws HibernateException {
        log.debug("Liste ID üretimi başlatıldı - Object: {}", object.getClass().getSimpleName());
        long startTime = System.currentTimeMillis();
        
        Connection connection = null;
        String generatedId = null;
        
        try {
            connection = session.getJdbcConnectionAccess().obtainConnection();
            log.debug("Veritabanı bağlantısı alındı - ID üretimi için stored procedure çağrılacak");
            
            try (CallableStatement stmt = connection.prepareCall("{call id_generator(?, ?)}")) {
                stmt.setString(1, "LST"); // Liste ID'leri için "LST" prefix'i
                stmt.registerOutParameter(2, Types.VARCHAR);
                
                log.debug("Stored procedure çağrılıyor - Prefix: 'LST'");
                stmt.execute();
                
                generatedId = stmt.getString(2);
                long duration = System.currentTimeMillis() - startTime;
                
                if (generatedId != null) {
                    log.info("✅ Liste ID başarıyla üretildi - GeneratedId: {}, Duration: {}ms", generatedId, duration);
                } else {
                    log.error("❌ ID üretimi başarısız - Stored procedure null ID döndürdü, Duration: {}ms", duration);
                }
            }
            
        } catch (SQLException e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ ID üretimi veritabanı hatası - Error: {}, Duration: {}ms", e.getMessage(), duration, e);
            throw new HibernateException("ID üretimi sırasında veritabanı hatası oluştu (ListIdGenerator): " + e.getMessage(), e);
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("❌ ID üretimi beklenmeyen hatası - Error: {}, Duration: {}ms", e.getMessage(), duration, e);
            throw new HibernateException("ID üretimi sırasında beklenmeyen hata oluştu (ListIdGenerator): " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    session.getJdbcConnectionAccess().releaseConnection(connection);
                    log.debug("Veritabanı bağlantısı kapatıldı");
                } catch (SQLException e) {
                    log.warn("⚠️ Veritabanı bağlantısı kapatılırken hata - Error: {}", e.getMessage());
                }
            }
        }
        
        if (generatedId == null) {
            log.error("❌ ID üretimi kritik hatası - Stored procedure null ID döndürdü");
            throw new HibernateException("ID üretimi başarısız oldu, stored procedure null ID döndürdü (ListIdGenerator)");
        }
        
        log.debug("Liste ID üretimi tamamlandı - GeneratedId: {}", generatedId);
        return generatedId;
    }
}