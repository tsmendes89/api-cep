package br.com.cep.model;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name="tcidade")
public class Cidade {

  @Id
  private String ibge;
  private String uf;
  private String localidade;

  @OneToMany(mappedBy = "cidade", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
  private List<Cep> ceps;

  public String getIbge() {
    return ibge;
  }

  public void setIbge(String ibge) {
    this.ibge = ibge;
  }

  public String getUf() {
    return uf;
  }

  public void setUf(String uf) {
    this.uf = uf;
  }

  public String getLocalidade() {
    return localidade;
  }

  public void setLocalidade(String localidade) {
    this.localidade = localidade;
  }

  public List<Cep> getCeps() {
    return ceps;
  }

  public void setCeps(List<Cep> ceps) {
    this.ceps = ceps;
  }
}
