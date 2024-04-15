package baop;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.Timestamp;

import com.sankhya.util.TimeUtils;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.Registro;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

public class Lancapedido implements AcaoRotinaJava {

	@Override
	public void doAction(ContextoAcao ctx) throws Exception {
		
		
		try {
			Registro linhas[] = ctx.getLinhas();
			BigDecimal codprod = BigDecimal.ZERO;
			BigDecimal qtdproduto = BigDecimal.ZERO;
			
			

			for (Registro linha : linhas) {

				qtdproduto = (BigDecimal) linha.getCampo("QTDNEG"); //QUANTIDADE DO DASH
				codprod = (BigDecimal) linha.getCampo("CODPROD"); //CODIGO DO PRODUTO
				int qtdselecionadaint = (int) ctx.getParam("QUANTIDADE"); 
				BigDecimal qtdselecionada = new BigDecimal(qtdselecionadaint);  //QUANTIDIDADE DIGITADA
				

				if (codprod!= null && qtdproduto.compareTo(BigDecimal.ZERO)> 0 && qtdselecionada.compareTo(qtdproduto) <= 0) {

					
						consultapedido(ctx, qtdselecionada, codprod, qtdproduto);}	//CONSULTA SE TEM PEDIDO EM ABERTO DA TOP 9169
					
			
				else {ctx.setMensagemRetorno("Quantidade selecionada é superior a quantidade disponível");}
			
			}
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			StringBuffer mensagem = new StringBuffer();
			e.printStackTrace(pw);
			mensagem.append("<b>Erro Exceção doAction: </b>" + e.getMessage() + sw.toString());
			ctx.setMensagemRetorno(mensagem.toString());
		}

	}

	private void consultapedido(ContextoAcao ctx, BigDecimal qtdselecionada, BigDecimal codprod, BigDecimal qtdproduto)
			throws Exception {
		
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		jdbc.openSession();
		NativeSql sql = new NativeSql(jdbc);
		NativeSql query = new NativeSql(jdbc);
		query.appendSql("SELECT MAX (NUNOTA) AS NUNOTA FROM TGFCAB WHERE CODTIPOPER = 9150 AND STATUSNOTA = 'A'");
		ResultSet rset = query.executeQuery();

		if (rset.next()) {
			BigDecimal nunota = rset.getBigDecimal("NUNOTA");

			// SE NÃO TIVER PEDIDO EM ABERTO LANÇA UM NOVO
			if (nunota == null) {
				addpedido(ctx,qtdselecionada, codprod, qtdproduto);}

			// SE TIVER PEDIDO EM ABERTO CONSULTA E ADICIONA ITENS
			if (nunota != null) {
				consultaitens(ctx,nunota,codprod,qtdselecionada, qtdproduto);
				
			}
		}

			NativeSql.releaseResources(sql);
			JdbcWrapper.closeSession(jdbc);
		}
	

	private void consultaitens(ContextoAcao ctx, BigDecimal nunota, BigDecimal produto, BigDecimal qtdselecionada, BigDecimal qtdproduto) throws Exception {
	
		Utilitarios util = new Utilitarios();
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		jdbc.openSession();
		NativeSql query = new NativeSql(jdbc);	
		query.setNamedParameter("NUNOTA", nunota);
		query.appendSql("SELECT CODPROD, QTDNEG FROM TGFITE WHERE NUNOTA = :NUNOTA");
		ResultSet rset = query.executeQuery();
		
	   boolean encontrouLinhaValida = false;
		
	   while (rset.next()) {
			BigDecimal codprod = rset.getBigDecimal("CODPROD");
			BigDecimal quantidade = rset.getBigDecimal("QTDNEG");
			
			 if (codprod.equals(produto)) {
				  encontrouLinhaValida = true;
			        util.updateqtd(ctx, nunota, codprod, quantidade, qtdselecionada);
			        break;
             }
         }
		
	   if (!encontrouLinhaValida) {
		   consultaPreco(ctx,  nunota,  qtdselecionada, produto, qtdproduto);
	   }
	   
	

	}
		
	

	private void addpedido(ContextoAcao ctx, BigDecimal qtdselecionada, BigDecimal codprod, BigDecimal qtdproduto) throws MGEModelException {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		NativeSql query = null;
		ResultSet rset = null;
		try {

			JapeWrapper cabDAO = JapeFactory.dao("CabecalhoNota");
			Timestamp dhAlterTop = null;
			hnd = JapeSession.open();
			hnd.setFindersMaxRows(-1);
			EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
			jdbc.openSession();
			query = new NativeSql(jdbc);
			query.setNamedParameter("CODTIPOPER", BigDecimal.valueOf(9150)); // CONSULTA ULTIMA DATA DE ALTERAÇÃO DA TOP
			query.appendSql("SELECT DHALTER FROM TGFTOP WHERE CODTIPOPER = :CODTIPOPER "
					+ "	AND DHALTER = (SELECT MAX(DHALTER) FROM TGFTOP WHERE CODTIPOPER = :CODTIPOPER)");
			rset = query.executeQuery();
			if (!rset.wasNull()) {
				while (rset.next()) {
					dhAlterTop = rset.getTimestamp("DHALTER");}}
			DynamicVO addPedido = cabDAO.create()
					.set("NUMNOTA", BigDecimal.valueOf(0))
					.set("CODEMP", BigDecimal.valueOf(1))
					.set("CODPARC", BigDecimal.valueOf(17255))
					.set("TIPMOV", 'V')
					.set("DTNEG", TimeUtils.getNow())
					.set("CODTIPOPER", BigDecimal.valueOf(9150))
					.set("DHTIPOPER", dhAlterTop)
					.set("STATUSNOTA", "A")
					.set("SERIENOTA", "1")
					.set("CODVEND", BigDecimal.valueOf(0))
					.set("CODNAT", BigDecimal.valueOf(80603))
					.set("CODCENCUS", BigDecimal.valueOf(13900))
					.set("CODTIPVENDA", BigDecimal.valueOf(148))
					.set("CODVEND", BigDecimal.valueOf(0))
					.set("DTENTSAI", TimeUtils.getNow())
					.set("DTMOV", TimeUtils.getNow())
					.set("DTFATUR", TimeUtils.getNow())
					.set("HRENTSAI", TimeUtils.getNow())
					.save();

			BigDecimal nunota = addPedido.asBigDecimal("NUNOTA"); // NUNOTA NOVO PEDIDO
			
			if(nunota!= null) {
				
				
				consultaPreco(ctx, nunota, qtdselecionada,codprod, qtdproduto);}

			

		} catch (Exception e) {
			MGEModelException.throwMe(e);
		} finally {
			JapeSession.close(hnd);
			JdbcWrapper.closeSession(jdbc);}
	}

	public void consultaPreco(ContextoAcao ctx, BigDecimal nunota, BigDecimal qtdselecionada,BigDecimal codprod, BigDecimal qtdproduto) throws MGEModelException {
		JapeSession.SessionHandle hnd = null;
		JdbcWrapper jdbc = null;
		NativeSql query = null;
		ResultSet rset = null;
		try {
			hnd = JapeSession.open();
			hnd.setFindersMaxRows(-1);
			EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
			jdbc.openSession();
			query = new NativeSql(jdbc);
			query.setNamedParameter("CODPROD", codprod);
			query.appendSql("SELECT VLRVENDA FROM TGFEXC, TGFTAB WHERE TGFTAB.NUTAB = TGFEXC.NUTAB AND TGFTAB.CODTAB = 6"
					+ "AND  DTVIGOR =  (SELECT MAX( DTVIGOR) FROM TGFTAB TAB1 WHERE TGFTAB.CODTAB = TAB1.CODTAB AND CODPROD = :CODPROD)");
			rset = query.executeQuery();
			if (rset.next()) {
				BigDecimal vlr = rset.getBigDecimal("VLRVENDA");
				
				
			if(vlr!= null) {
				additens(ctx, nunota,qtdselecionada,codprod, vlr, qtdproduto);}
			}

		} catch (Exception e) {
			e.printStackTrace();
			MGEModelException.throwMe(e);
			System.out.println("Erro ao Executar consultaPreco" + e.getCause() + e.getMessage());
		} finally {
			JdbcWrapper.closeSession(jdbc);
			JapeSession.close(hnd);
		}

	}
public void additens(ContextoAcao ctx, BigDecimal nunota, BigDecimal qtdselecionada,BigDecimal codprod, BigDecimal vlr,
BigDecimal qtdproduto) throws Exception {
		JapeSession.SessionHandle hnd = null;
		Utilitarios add = new Utilitarios();
		ImpostosHelpper imp = new ImpostosHelpper();
		
		try {
			hnd = JapeSession.open();

			JapeWrapper proDAO = JapeFactory.dao("Produto");
			JapeWrapper iteDAO = JapeFactory.dao("ItemNota");

			        iteDAO.create()
					.set("CODEMP", BigDecimal.valueOf(1))
					.set("CODPROD", codprod)
					.set("NUNOTA", nunota)
					.set("QTDNEG", qtdselecionada)
					.set("CODVOL", proDAO.findByPK(codprod).asString("CODVOL"))
					.set("VLRUNIT", vlr)
					.set("VLRTOT", vlr.multiply(qtdselecionada))
					.set("CODLOCALORIG", BigDecimal.valueOf(1030000))
					.set("RESERVA", "N")
					.set("ATUALESTOQUE", BigDecimal.valueOf(-1))
					.set("CODVEND",BigDecimal.valueOf(0))
					.set("VLRDESC", BigDecimal.valueOf(0.00))
					.set("CODCFO", BigDecimal.valueOf(5905))
					.set("CODTRIB", BigDecimal.valueOf(41))
					.set("PERCDESC", BigDecimal.valueOf(0.00))
					.set("BASEICMS", BigDecimal.valueOf(0.00))
					.set("VLRICMS",  BigDecimal.valueOf(0.00))
					.set("ALIQICMS",  BigDecimal.valueOf(0.00))
					.set("BASEIPI",  BigDecimal.valueOf(0.00))
					.set("VLRIPI",  BigDecimal.valueOf(0.00))
					.set("ALIQIPI",  BigDecimal.valueOf(0.00))
					.set("CODLOCALTERC",  BigDecimal.valueOf(1030000))
					.set("ATUALESTTERC", "P")
					.set("TERCEIROS", "S")
					.set("PENDENTE",  "S")
					.set("AD_REMESSAORIGEM", BigDecimal.valueOf(1022146))
			        .save();
			
			
			add.atualizaitens(ctx, nunota, codprod, qtdselecionada);
			imp.calcularImpostos(nunota);
		    imp.setForcarRecalculo(true);
		    imp.totalizarNota(nunota);
			ctx.setMensagemRetorno("Itens adicionados com sucesso no pedido: "+nunota);
			
			}
		
		 catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);

		} finally {
		
			JapeSession.close(hnd);
		}
		
		
		
	}





}
