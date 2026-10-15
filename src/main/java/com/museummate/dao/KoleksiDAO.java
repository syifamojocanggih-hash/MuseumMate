package com.museummate.dao;

import com.museummate.model.Artefak;
import com.museummate.model.Fosil;
import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.Lukisan;
import com.museummate.model.Naskah;
import com.museummate.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object untuk operasi tabel koleksi dan sub-tabel kategori secara polimorfik.
 */
public class KoleksiDAO {

    private static final String BASE_SELECT_SQL =
            "SELECT k.*, " +
            "l.seniman, l.teknik_media, l.dimensi, l.ada_bingkai, " +
            "a.era_dinasti, a.fungsi_objek, a.inskripsi_ornamen, " +
            "n.bahasa, n.aksara, n.alas_naskah, n.jumlah_halaman, " +
            "f.nama_ilmiah_taksa, f.bagian_anatomi, f.formasi_geologi " +
            "FROM koleksi k " +
            "LEFT JOIN koleksi_lukisan l ON k.id_koleksi = l.id_koleksi " +
            "LEFT JOIN koleksi_artefak a ON k.id_koleksi = a.id_koleksi " +
            "LEFT JOIN koleksi_naskah n ON k.id_koleksi = n.id_koleksi " +
            "LEFT JOIN koleksi_fosil f ON k.id_koleksi = f.id_koleksi ";

    public List<Koleksi> findAll() throws SQLException {
        List<Koleksi> list = new ArrayList<>();
        String sql = BASE_SELECT_SQL + "ORDER BY k.id_koleksi DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapResultSetToKoleksi(rs));
            }
        }
        return list;
    }

    public Koleksi findById(int id) throws SQLException {
        String sql = BASE_SELECT_SQL + "WHERE k.id_koleksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKoleksi(rs);
                }
            }
        }
        return null;
    }

    public Koleksi findByKode(String kodeRegistrasi) throws SQLException {
        String sql = BASE_SELECT_SQL + "WHERE k.kode_registrasi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kodeRegistrasi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToKoleksi(rs);
                }
            }
        }
        return null;
    }

    public List<Koleksi> searchAndFilter(String keyword, KategoriKoleksi kategori, KondisiKoleksi kondisi) throws SQLException {
        List<Koleksi> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(BASE_SELECT_SQL).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (k.nama LIKE ? OR k.kode_registrasi LIKE ? OR k.asal LIKE ? OR k.bahan LIKE ?) ");
            String term = "%" + keyword.trim() + "%";
            params.add(term);
            params.add(term);
            params.add(term);
            params.add(term);
        }

        if (kategori != null) {
            sql.append("AND k.kategori = ? ");
            params.add(kategori.name());
        }

        if (kondisi != null) {
            sql.append("AND k.kondisi = ? ");
            params.add(kondisi.name());
        }

        sql.append("ORDER BY k.id_koleksi DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToKoleksi(rs));
                }
            }
        }
        return list;
    }

    public boolean save(Koleksi k) throws SQLException {
        String sqlMain = "INSERT INTO koleksi (kode_registrasi, nama, kategori, asal, tahun_perkiraan, bahan, lokasi_penyimpanan, sumber_perolehan, kondisi, tanggal_diterima) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int generatedId = -1;
                try (PreparedStatement ps = conn.prepareStatement(sqlMain, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, k.getKodeRegistrasi());
                    ps.setString(2, k.getNama());
                    ps.setString(3, k.getKategori().name());
                    ps.setString(4, k.getAsal());
                    ps.setString(5, k.getTahunPerkiraan());
                    ps.setString(6, k.getBahan());
                    ps.setString(7, k.getLokasiPenyimpanan());
                    ps.setString(8, k.getSumberPerolehan());
                    ps.setString(9, k.getKondisi().name());
                    ps.setDate(10, Date.valueOf(k.getTanggalDiterima()));

                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            generatedId = rs.getInt(1);
                            k.setIdKoleksi(generatedId);
                        }
                    }
                }

                if (generatedId > 0) {
                    saveSubclassAttributes(conn, k);
                }

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean update(Koleksi k) throws SQLException {
        String sqlMain = "UPDATE koleksi SET kode_registrasi = ?, nama = ?, kategori = ?, asal = ?, tahun_perkiraan = ?, " +
                         "bahan = ?, lokasi_penyimpanan = ?, sumber_perolehan = ?, kondisi = ?, tanggal_diterima = ? " +
                         "WHERE id_koleksi = ?";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlMain)) {
                    ps.setString(1, k.getKodeRegistrasi());
                    ps.setString(2, k.getNama());
                    ps.setString(3, k.getKategori().name());
                    ps.setString(4, k.getAsal());
                    ps.setString(5, k.getTahunPerkiraan());
                    ps.setString(6, k.getBahan());
                    ps.setString(7, k.getLokasiPenyimpanan());
                    ps.setString(8, k.getSumberPerolehan());
                    ps.setString(9, k.getKondisi().name());
                    ps.setDate(10, Date.valueOf(k.getTanggalDiterima()));
                    ps.setInt(11, k.getIdKoleksi());

                    ps.executeUpdate();
                }

                saveSubclassAttributes(conn, k);

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public boolean updateKondisi(int idKoleksi, KondisiKoleksi kondisiBaru) throws SQLException {
        String sql = "UPDATE koleksi SET kondisi = ? WHERE id_koleksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kondisiBaru.name());
            ps.setInt(2, idKoleksi);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int idKoleksi) throws SQLException {
        String sql = "DELETE FROM koleksi WHERE id_koleksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKoleksi);
            return ps.executeUpdate() > 0;
        }
    }

    public int countPemeriksaan(int idKoleksi) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pemeriksaan WHERE id_koleksi = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idKoleksi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private void saveSubclassAttributes(Connection conn, Koleksi k) throws SQLException {
        if (k instanceof Lukisan lukisan) {
            String sql = "INSERT INTO koleksi_lukisan (id_koleksi, seniman, teknik_media, dimensi, ada_bingkai) " +
                         "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                         "seniman = VALUES(seniman), teknik_media = VALUES(teknik_media), dimensi = VALUES(dimensi), ada_bingkai = VALUES(ada_bingkai)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, lukisan.getIdKoleksi());
                ps.setString(2, lukisan.getSeniman());
                ps.setString(3, lukisan.getTeknikMedia());
                ps.setString(4, lukisan.getDimensi());
                ps.setBoolean(5, lukisan.isAdaBingkai());
                ps.executeUpdate();
            }
        } else if (k instanceof Artefak artefak) {
            String sql = "INSERT INTO koleksi_artefak (id_koleksi, era_dinasti, fungsi_objek, inskripsi_ornamen) " +
                         "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                         "era_dinasti = VALUES(era_dinasti), fungsi_objek = VALUES(fungsi_objek), inskripsi_ornamen = VALUES(inskripsi_ornamen)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, artefak.getIdKoleksi());
                ps.setString(2, artefak.getEraDinasti());
                ps.setString(3, artefak.getFungsiObjek());
                ps.setString(4, artefak.getInskripsiOrnamen());
                ps.executeUpdate();
            }
        } else if (k instanceof Naskah naskah) {
            String sql = "INSERT INTO koleksi_naskah (id_koleksi, bahasa, aksara, alas_naskah, jumlah_halaman) " +
                         "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                         "bahasa = VALUES(bahasa), aksara = VALUES(aksara), alas_naskah = VALUES(alas_naskah), jumlah_halaman = VALUES(jumlah_halaman)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, naskah.getIdKoleksi());
                ps.setString(2, naskah.getBahasa());
                ps.setString(3, naskah.getAksara());
                ps.setString(4, naskah.getAlasNaskah());
                ps.setInt(5, naskah.getJumlahHalaman());
                ps.executeUpdate();
            }
        } else if (k instanceof Fosil fosil) {
            String sql = "INSERT INTO koleksi_fosil (id_koleksi, nama_ilmiah_taksa, bagian_anatomi, formasi_geologi) " +
                         "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE " +
                         "nama_ilmiah_taksa = VALUES(nama_ilmiah_taksa), bagian_anatomi = VALUES(bagian_anatomi), formasi_geologi = VALUES(formasi_geologi)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, fosil.getIdKoleksi());
                ps.setString(2, fosil.getNamaIlmiahTaksa());
                ps.setString(3, fosil.getBagianAnatomi());
                ps.setString(4, fosil.getFormasiGeologi());
                ps.executeUpdate();
            }
        }
    }

    private Koleksi mapResultSetToKoleksi(ResultSet rs) throws SQLException {
        KategoriKoleksi kategori = KategoriKoleksi.fromString(rs.getString("kategori"));
        Koleksi k;

        switch (kategori) {
            case LUKISAN -> {
                Lukisan l = new Lukisan();
                l.setSeniman(rs.getString("seniman"));
                l.setTeknikMedia(rs.getString("teknik_media"));
                l.setDimensi(rs.getString("dimensi"));
                l.setAdaBingkai(rs.getBoolean("ada_bingkai"));
                k = l;
            }
            case ARTEFAK -> {
                Artefak a = new Artefak();
                a.setEraDinasti(rs.getString("era_dinasti"));
                a.setFungsiObjek(rs.getString("fungsi_objek"));
                a.setInskripsiOrnamen(rs.getString("inskripsi_ornamen"));
                k = a;
            }
            case NASKAH -> {
                Naskah n = new Naskah();
                n.setBahasa(rs.getString("bahasa"));
                n.setAksara(rs.getString("aksara"));
                n.setAlasNaskah(rs.getString("alas_naskah"));
                n.setJumlahHalaman(rs.getInt("jumlah_halaman"));
                k = n;
            }
            case FOSIL -> {
                Fosil f = new Fosil();
                f.setNamaIlmiahTaksa(rs.getString("nama_ilmiah_taksa"));
                f.setBagianAnatomi(rs.getString("bagian_anatomi"));
                f.setFormasiGeologi(rs.getString("formasi_geologi"));
                k = f;
            }
            default -> throw new IllegalStateException("Kategori tidak valid: " + kategori);
        }

        k.setIdKoleksi(rs.getInt("id_koleksi"));
        k.setKodeRegistrasi(rs.getString("kode_registrasi"));
        k.setNama(rs.getString("nama"));
        k.setKategori(kategori);
        k.setAsal(rs.getString("asal"));
        k.setTahunPerkiraan(rs.getString("tahun_perkiraan"));
        k.setBahan(rs.getString("bahan"));
        k.setLokasiPenyimpanan(rs.getString("lokasi_penyimpanan"));
        k.setSumberPerolehan(rs.getString("sumber_perolehan"));
        k.setKondisi(KondisiKoleksi.fromString(rs.getString("kondisi")));

        Date tglDiterima = rs.getDate("tanggal_diterima");
        if (tglDiterima != null) {
            k.setTanggalDiterima(tglDiterima.toLocalDate());
        }

        Timestamp dibuatPada = rs.getTimestamp("dibuat_pada");
        if (dibuatPada != null) {
            k.setDibuatPada(dibuatPada.toLocalDateTime());
        }

        Timestamp diperbaruiPada = rs.getTimestamp("diperbarui_pada");
        if (diperbaruiPada != null) {
            k.setDiperbaruiPada(diperbaruiPada.toLocalDateTime());
        }

        return k;
    }
}
