package baop;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.jape.core.JapeSession.SessionHandle;
import br.com.sankhya.jape.dao.JdbcWrapper;
import br.com.sankhya.jape.sql.NativeSql;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.jape.wrapper.JapeFactory;
import br.com.sankhya.jape.wrapper.JapeWrapper;
import br.com.sankhya.mgecomercial.model.facades.helpper.FaturamentoHelper;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.auth.AuthenticationInfo;
import br.com.sankhya.modelcore.comercial.BarramentoRegra;
import br.com.sankhya.modelcore.comercial.CentralFaturamento;
import br.com.sankhya.modelcore.comercial.ConfirmacaoNotaHelper;
import br.com.sankhya.modelcore.comercial.impostos.ImpostosHelpper;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class Utilitarios {

	public void updateqtd(ContextoAcao ctx, BigDecimal nunota, BigDecimal codprod, BigDecimal quantidade,
			BigDecimal qtdselecionada) throws Exception {

		BigDecimal novavariavel = quantidade.add(qtdselecionada); // SOMA DO VALOR ANTERIOR ADICIONADO DO ATUAL

		ImpostosHelpper imp = new ImpostosHelpper();
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		jdbc.openSession();

		NativeSql query = new NativeSql(jdbc);
		query.setNamedParameter("NUNOTA", nunota);
		query.setNamedParameter("CODPROD", codprod);
		query.appendSql("SELECT VLRUNIT FROM TGFITE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
		ResultSet rset = query.executeQuery();

		if (rset.next()) {
			BigDecimal vlrunitario = rset.getBigDecimal("VLRUNIT");

			try {

				NativeSql sql = new NativeSql(jdbc);
				sql.appendSql(
						"UPDATE TGFITE SET QTDNEG = :QUANTIDADE, VLRTOT = :VLRTOT WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
				sql.setNamedParameter("NUNOTA", nunota);
				sql.setNamedParameter("VLRTOT", vlrunitario.multiply(novavariavel));
				sql.setNamedParameter("QUANTIDADE", novavariavel);
				sql.setNamedParameter("CODPROD", codprod);
				sql.executeUpdate();

				imp.calcularImpostos(nunota);
				imp.setForcarRecalculo(true);
				imp.totalizarNota(nunota);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				JdbcWrapper.closeSession(jdbc);
			}
		}

		atualizaitens(ctx, nunota, codprod, qtdselecionada);
		ctx.setMensagemRetorno("Quantidade adicionada com sucesso no pedido: " + nunota);

	}

	public void atualizaitens(ContextoAcao ctx, BigDecimal nunota, BigDecimal codprod, BigDecimal qtdselecionada)
			throws Exception {
		ImpostosHelpper imp = new ImpostosHelpper();
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		jdbc.openSession();

		NativeSql query = new NativeSql(jdbc);
		query.setNamedParameter("CODPROD", codprod);
		query.setNamedParameter("NUNOTAORIGEM", BigDecimal.valueOf(1022146)); // CONSULTA A QUATIDADE DO PRODUTO NO
																				// PEDIDO 24
		query.appendSql("SELECT QTDNEG, SEQUENCIA FROM TGFITE WHERE NUNOTA = :NUNOTAORIGEM AND CODPROD = :CODPROD");
		ResultSet rset = query.executeQuery();

		if (rset.next()) {
			int qtdneg = rset.getInt("QTDNEG");
			BigDecimal sequencia = rset.getBigDecimal("SEQUENCIA");

			if (qtdneg - qtdselecionada.intValue() == 0) {
				// SE A QUANTIDADE SELECIONADA NO DASH FOR IGUAL A QUE TEM NO PEDIDO 24, DELETA
				// O ITEM NO PEDIDO 24
				JapeSession.SessionHandle hnd = null;
				try {
					hnd = JapeSession.open(); //ABERTURA DE SESSÃO
					
					hnd.execWithTX( new JapeSession.TXBlock(){//BLOCO DE TRANSAÇÃO MANUAL
		        		public void doWithTx() throws Exception{//BLOCO DE TRANSAÇÃO MANUAL
				JapeFactory.dao(DynamicEntityNames.ITEM_NOTA).delete(BigDecimal.valueOf(1022146), sequencia);

				imp.calcularImpostos(BigDecimal.valueOf(1022146));
				imp.setForcarRecalculo(true);
				imp.totalizarNota(BigDecimal.valueOf(1022146));
		        		}
		        		
		    			;}); 


					JapeSession.close(hnd);
				}catch (Exception e) {
					MGEModelException.throwMe(new Exception(e));}

					finally { 
						 
					}}

			else {
				// SE FOR DIFERENTE O NOVO VALOR É A QTDNEG DO PEDIDO 24 - O VALOR SELECIONADO
				int novavariavel = qtdneg - qtdselecionada.intValue();

				try {
					NativeSql sql = new NativeSql(jdbc);
					sql.appendSql(
							"UPDATE TGFITE SET QTDNEG = :QUANTIDADE, AD_APONTAMENTO =:APONTAMENTO WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
					sql.setNamedParameter("NUNOTA", BigDecimal.valueOf(1022146));
					sql.setNamedParameter("QUANTIDADE", novavariavel);
					sql.setNamedParameter("CODPROD", codprod);
					sql.setNamedParameter("APONTAMENTO", nunota);
					sql.executeUpdate();

					atualizavlrtot(ctx, BigDecimal.valueOf(1022146)); // ATUALIZAR O VLRTOT DA ITE DO PEDIDO 24 PARA
																		// TOTALIZAR O VLRNOTA

				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					JdbcWrapper.closeSession(jdbc);
				
				}

			}

		}

	}

	private void atualizavlrtot(ContextoAcao ctx, BigDecimal nunota24) throws Exception {
		ImpostosHelpper imp = new ImpostosHelpper();
		
		EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		JdbcWrapper jdbc = entity.getJdbcWrapper();
		jdbc.openSession();

		NativeSql query = new NativeSql(jdbc);
		query.setNamedParameter("NUNOTA", nunota24);
		query.appendSql("SELECT CODPROD, QTDNEG, VLRUNIT,SEQUENCIA FROM TGFITE WHERE NUNOTA = :NUNOTA");
		ResultSet rset = query.executeQuery();

		while (rset.next()) {
			BigDecimal qtdneg = rset.getBigDecimal("QTDNEG");
			BigDecimal vlrunit = rset.getBigDecimal("VLRUNIT");
			BigDecimal sequencia = rset.getBigDecimal("SEQUENCIA");

			JapeSession.SessionHandle hnd = null;
			try {
				hnd = JapeSession.open(); //ABERTURA DE SESSÃO
				
				hnd.execWithTX( new JapeSession.TXBlock(){//BLOCO DE TRANSAÇÃO MANUAL
	        		public void doWithTx() throws Exception{//BLOCO DE TRANSAÇÃO MANUAL
	        			
	        		
				JapeFactory.dao("ItemNota").prepareToUpdateByPK(nunota24, sequencia)
						.set("VLRTOT", qtdneg.multiply(vlrunit)).update();

				imp.calcularImpostos(nunota24);
				imp.setForcarRecalculo(true);
				imp.totalizarNota(nunota24);
	        		}
	        		
	    			;});
				JapeSession.close(hnd);

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				JdbcWrapper.closeSession(jdbc);
				
			}
		}

	}


	   
	   

}
