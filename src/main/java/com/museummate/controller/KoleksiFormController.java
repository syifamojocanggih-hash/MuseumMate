package com.museummate.controller;

import com.museummate.model.Artefak;
import com.museummate.model.Fosil;
import com.museummate.model.KategoriKoleksi;
import com.museummate.model.Koleksi;
import com.museummate.model.KondisiKoleksi;
import com.museummate.model.Lukisan;
import com.museummate.model.Naskah;
import com.museummate.service.KoleksiService;
import com.museummate.util.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * Controller untuk dialog input dan ubah data koleksi (PRD F2 & F3).
 * Menampilkan form atribut spesifik sesuai kategori terpilih (OOP Polimorfisme).
 */
public class KoleksiFormController {

    @FXML private Label lblFormTitle;
    @FXML private TextField txtKodeRegistrasi;
    @FXML private ComboBox<KategoriKoleksi> cmbKategori;
    @FXML private TextField txtNama;
    @FXML private ComboBox<KondisiKoleksi> cmbKondisi;
    @FXML private DatePicker dpTanggalDiterima;
    @FXML private TextField txtAsal;
    @FXML private TextField txtTahunPerkiraan;
    @FXML private TextField txtBahan;
    @FXML private TextField txtSumberPerolehan;
    @FXML private TextField txtLokasiPenyimpanan;

    // Subform Lukisan
    @FXML private GridPane gridLukisan;
    @FXML private TextField txtSeniman;
    @FXML private TextField txtTeknikMedia;
    @FXML private TextField txtDimensi;
    @FXML private CheckBox chkAdaBingkai;

    // Subform Artefak
    @FXML private GridPane gridArtefak;
    @FXML private TextField txtEraDinasti;
    @FXML private TextField txtFungsiObjek;
    @FXML private TextField txtInskripsiOrnamen;

    // Subform Naskah
    @FXML private GridPane gridNaskah;
    @FXML private TextField txtBahasa;
    @FXML private TextField txtAksara;
    @FXML private TextField txtAlasNaskah;
    @FXML private Spinner<Integer> spnJumlahHalaman;

    // Subform Fosil
    @FXML private GridPane gridFosil;
    @FXML private TextField txtNamaIlmiahTaksa;
    @FXML private TextField txtBagianAnatomi;
    @FXML private TextField txtFormasiGeologi;

    @FXML private Button btnSimpan;

    private final KoleksiService koleksiService = new KoleksiService();
    private Koleksi existingKoleksi;
    private Runnable onSuccessCallback;

    @FXML
    public void initialize() {
        cmbKategori.getItems().addAll(KategoriKoleksi.values());
        cmbKondisi.getItems().addAll(KondisiKoleksi.values());
        dpTanggalDiterima.setValue(LocalDate.now());

        spnJumlahHalaman.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1));

        // Switch subform visibility based on chosen category
        cmbKategori.valueProperty().addListener((obs, oldVal, newVal) -> updateSubformVisibility(newVal));
        cmbKategori.setValue(KategoriKoleksi.LUKISAN);
    }

    public void setKoleksi(Koleksi koleksi) {
        this.existingKoleksi = koleksi;
        if (koleksi != null) {
            lblFormTitle.setText("Ubah Data Koleksi");
            txtKodeRegistrasi.setText(koleksi.getKodeRegistrasi());
            txtNama.setText(koleksi.getNama());
            cmbKategori.setValue(koleksi.getKategori());
            cmbKategori.setDisable(true); // Kategori tidak dapat diubah setelah dibuat
            cmbKondisi.setValue(koleksi.getKondisi());
            dpTanggalDiterima.setValue(koleksi.getTanggalDiterima());
            txtAsal.setText(koleksi.getAsal());
            txtTahunPerkiraan.setText(koleksi.getTahunPerkiraan());
            txtBahan.setText(koleksi.getBahan());
            txtSumberPerolehan.setText(koleksi.getSumberPerolehan());
            txtLokasiPenyimpanan.setText(koleksi.getLokasiPenyimpanan());

            // Isi nilai atribut khusus
            if (koleksi instanceof Lukisan l) {
                txtSeniman.setText(l.getSeniman());
                txtTeknikMedia.setText(l.getTeknikMedia());
                txtDimensi.setText(l.getDimensi());
                chkAdaBingkai.setSelected(l.isAdaBingkai());
            } else if (koleksi instanceof Artefak a) {
                txtEraDinasti.setText(a.getEraDinasti());
                txtFungsiObjek.setText(a.getFungsiObjek());
                txtInskripsiOrnamen.setText(a.getInskripsiOrnamen());
            } else if (koleksi instanceof Naskah n) {
                txtBahasa.setText(n.getBahasa());
                txtAksara.setText(n.getAksara());
                txtAlasNaskah.setText(n.getAlasNaskah());
                spnJumlahHalaman.getValueFactory().setValue(n.getJumlahHalaman());
            } else if (koleksi instanceof Fosil f) {
                txtNamaIlmiahTaksa.setText(f.getNamaIlmiahTaksa());
                txtBagianAnatomi.setText(f.getBagianAnatomi());
                txtFormasiGeologi.setText(f.getFormasiGeologi());
            }
        }
    }

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    private void updateSubformVisibility(KategoriKoleksi kategori) {
        gridLukisan.setVisible(false);
        gridLukisan.setManaged(false);
        gridArtefak.setVisible(false);
        gridArtefak.setManaged(false);
        gridNaskah.setVisible(false);
        gridNaskah.setManaged(false);
        gridFosil.setVisible(false);
        gridFosil.setManaged(false);

        if (kategori == null) return;

        switch (kategori) {
            case LUKISAN -> {
                gridLukisan.setVisible(true);
                gridLukisan.setManaged(true);
            }
            case ARTEFAK -> {
                gridArtefak.setVisible(true);
                gridArtefak.setManaged(true);
            }
            case NASKAH -> {
                gridNaskah.setVisible(true);
                gridNaskah.setManaged(true);
            }
            case FOSIL -> {
                gridFosil.setVisible(true);
                gridFosil.setManaged(true);
            }
        }
    }

    @FXML
    public void handleSimpan() {
        try {
            KategoriKoleksi kategori = cmbKategori.getValue();
            if (kategori == null) {
                AlertHelper.showWarning("Validasi", "Kategori koleksi harus dipilih.");
                return;
            }

            Koleksi koleksi;
            boolean isEdit = (existingKoleksi != null);

            if (isEdit) {
                koleksi = existingKoleksi;
            } else {
                // Polimorfisme: Instansiasi class turunan sesuai kategori
                koleksi = switch (kategori) {
                    case LUKISAN -> new Lukisan();
                    case ARTEFAK -> new Artefak();
                    case NASKAH -> new Naskah();
                    case FOSIL -> new Fosil();
                };
            }

            // Set Data Inti
            koleksi.setKodeRegistrasi(txtKodeRegistrasi.getText());
            koleksi.setNama(txtNama.getText());
            koleksi.setKategori(kategori);
            koleksi.setKondisi(cmbKondisi.getValue());
            koleksi.setTanggalDiterima(dpTanggalDiterima.getValue());
            koleksi.setAsal(txtAsal.getText());
            koleksi.setTahunPerkiraan(txtTahunPerkiraan.getText());
            koleksi.setBahan(txtBahan.getText());
            koleksi.setSumberPerolehan(txtSumberPerolehan.getText());
            koleksi.setLokasiPenyimpanan(txtLokasiPenyimpanan.getText());

            // Set Atribut Khusus
            if (koleksi instanceof Lukisan l) {
                l.setSeniman(txtSeniman.getText());
                l.setTeknikMedia(txtTeknikMedia.getText());
                l.setDimensi(txtDimensi.getText());
                l.setAdaBingkai(chkAdaBingkai.isSelected());
            } else if (koleksi instanceof Artefak a) {
                a.setEraDinasti(txtEraDinasti.getText());
                a.setFungsiObjek(txtFungsiObjek.getText());
                a.setInskripsiOrnamen(txtInskripsiOrnamen.getText());
            } else if (koleksi instanceof Naskah n) {
                n.setBahasa(txtBahasa.getText());
                n.setAksara(txtAksara.getText());
                n.setAlasNaskah(txtAlasNaskah.getText());
                n.setJumlahHalaman(spnJumlahHalaman.getValue());
            } else if (koleksi instanceof Fosil f) {
                f.setNamaIlmiahTaksa(txtNamaIlmiahTaksa.getText());
                f.setBagianAnatomi(txtBagianAnatomi.getText());
                f.setFormasiGeologi(txtFormasiGeologi.getText());
            }

            // Simpan ke Basis Data
            if (isEdit) {
                koleksiService.ubahKoleksi(koleksi);
                com.museummate.service.LogAktivitasService.catat("UBAH", "koleksi", 
                        "Memperbarui koleksi: " + koleksi.getKodeRegistrasi() + " - " + koleksi.getNama());
                AlertHelper.showInfo("Sukses", "Data koleksi berhasil diperbarui.");
            } else {
                koleksiService.tambahKoleksi(koleksi);
                com.museummate.service.LogAktivitasService.catat("TAMBAH", "koleksi", 
                        "Menambahkan koleksi baru: " + koleksi.getKodeRegistrasi() + " - " + koleksi.getNama());
                AlertHelper.showInfo("Sukses", "Koleksi baru berhasil disimpan.");
            }

            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }

            tutupDialog();
        } catch (IllegalArgumentException e) {
            AlertHelper.showWarning("Validasi Input", e.getMessage());
        } catch (Exception e) {
            AlertHelper.showError("Kesalahan Penyimpanan", "Gagal menyimpan koleksi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBatal() {
        tutupDialog();
    }

    private void tutupDialog() {
        Stage stage = (Stage) btnSimpan.getScene().getWindow();
        stage.close();
    }
}
