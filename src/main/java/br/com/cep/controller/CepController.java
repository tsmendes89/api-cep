package br.com.cep.controller;

import br.com.cep.dao.CepRepository;
import br.com.cep.dao.CidadeRepository;
import br.com.cep.model.Cep;
import br.com.cep.model.Cidade;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

@RestController
public class CepController {

	private CepRepository cepRepository;
	private CidadeRepository cidadeRepository;

	CepController(CepRepository cepRepository, CidadeRepository cidadeRepository){
		this.cepRepository = cepRepository;
		this.cidadeRepository = cidadeRepository;
	}

	@GetMapping("/cep/{cep}")
	public String findCep(@PathVariable(value = "cep") String cep) {

		String jsonViaCep = null;
		Cep cepMapeado = null;

		cep = cep.substring(0, 5) + '-' + cep.substring(5, 8);

		ResponseEntity<Cep> cepResult =  cepRepository.findById(cep)
				.map(record -> ResponseEntity.ok().body(record))
				.orElse(ResponseEntity.notFound().build());

		if (cepResult.getBody() == null){
			jsonViaCep = getViaCep(cep);
			JSONObject jsonObject = new JSONObject(jsonViaCep);
			cepMapeado = mapearJsonParaCep(jsonObject);
			salvar(cepMapeado);
			return mapearCepParaJson(cepMapeado);
		}
		return mapearCepParaJson(cepResult.getBody());
	}

	@GetMapping("/ceps")
	public String obterListaCepsCidade(@RequestParam(value = "ibge") String ibge, @RequestParam(required = false, value = "uf") String uf) {

		ResponseEntity<Cidade> cidadeResult =  cidadeRepository.findById(ibge)
				.map(record -> ResponseEntity.ok().body(record))
				.orElse(ResponseEntity.notFound().build());

		if (cidadeResult.getBody() != null) {
			return mapearCidadeParaJson(cidadeResult.getBody());
		}

		return null;
	}

	private String getViaCep(String cep){
		String json = null;
		String endereco = "http://viacep.com.br/ws/" + cep + "/json/";
		try {
			URL url = new URL (endereco);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : "
						+ conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));

			String output;

			while ((output = br.readLine()) != null) {
				if (json == null){
					json = output;
				} else {
					json = json + output;
				}
			}
			conn.disconnect();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return json;
	}

	private Cep mapearJsonParaCep(JSONObject jsonObject){
		Cep cep = new Cep();
		Cidade cidade = new Cidade();

		cep.setCep(jsonObject.getString("cep"));
		cep.setLogradouro(jsonObject.getString("logradouro"));
		cep.setComplemento(jsonObject.getString("complemento"));
		cep.setBairro(jsonObject.getString("bairro"));
		cep.setIbge(jsonObject.getString("ibge"));
		cidade.setIbge(jsonObject.getString("ibge"));
		cidade.setUf(jsonObject.getString("uf"));
		cidade.setLocalidade(jsonObject.getString("localidade"));

		cep.setCidade(cidade);

		return cep;
	};

	private String mapearCepParaJson(Cep cep){

		JSONObject jsonObject = new JSONObject();
		JSONObject cidade = new JSONObject();

		cidade.put("ibge", cep.getCidade().getIbge());
		cidade.put("uf", cep.getCidade().getUf());
		cidade.put("localidade", cep.getCidade().getLocalidade());

		jsonObject.put("cidade", cidade);
		jsonObject.put("cep", cep.getCep());
		jsonObject.put("logradouro", cep.getLogradouro());
		jsonObject.put("complemento", cep.getComplemento());
		jsonObject.put("bairro", cep.getBairro());

		return jsonObject.toString();
	}

	private String mapearCidadeParaJson(Cidade cidade){
		JSONObject jsonObject = new JSONObject();
		JSONArray ceps = new JSONArray();

		for (int i = 0; i < cidade.getCeps().size(); i++){
			JSONObject cep = new JSONObject();
			cep.put("cep", cidade.getCeps().get(i).getCep());
			cep.put("logradouro", cidade.getCeps().get(i).getLogradouro());
			cep.put("bairro", cidade.getCeps().get(i).getBairro());
			ceps.put(i, cep);
		}

		jsonObject.put("ibge", cidade.getIbge());
		jsonObject.put("uf", cidade.getUf());
		jsonObject.put("localidade", cidade.getLocalidade());
		jsonObject.put("ceps", ceps);

		return jsonObject.toString();
	}


	private Cep salvar(Cep cep)
	{
		this.cidadeRepository.save(cep.getCidade());
		return this.cepRepository.save(cep);
	}

}
