package eventoop;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
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

public class Adicionaitem {
	
	
	  public void consultaPreco(BigDecimal nunota, BigDecimal codprod, BigDecimal quantidade, BigDecimal nunotaorigem, BigDecimal nunpedevox) throws MGEModelException {

			JdbcWrapper jdbc = null;
			EntityFacade entity = EntityFacadeFactory.getDWFFacade();
			jdbc = entity.getJdbcWrapper();
		    
			try {
				
				jdbc.openSession();
			    NativeSql query = new NativeSql(jdbc);
				query.setNamedParameter("CODPROD", codprod);
				query.appendSql("SELECT VLRVENDA FROM TGFEXC, TGFTAB WHERE\r\n"
						+ "TGFTAB.NUTAB = TGFEXC.NUTAB AND TGFTAB.CODTAB = 6\r\n"
						+ "AND  DTVIGOR =(SELECT MAX( DTVIGOR) FROM TGFTAB TAB1 WHERE TGFTAB.CODTAB = TAB1.CODTAB)"
						+ "AND CODPROD = :CODPROD"); //CODIGO PRODUTO
				
				ResultSet rset = query.executeQuery();
				if (rset.next()) {
					BigDecimal vlr = rset.getBigDecimal("VLRVENDA");
					if(null==nunpedevox) {
					 adicionaitens(nunota, codprod, quantidade, vlr, nunotaorigem);
					}else {
						adicionaitensevox(nunota, codprod, quantidade, vlr, nunotaorigem,nunpedevox);
					}
					
				}
					
				
			}

			catch (Exception e) {
				e.printStackTrace();
				MGEModelException.throwMe(e);
				System.out.println("Erro ao Executar Evento consultaPreco" + e.getCause() + e.getMessage());
			} finally {
				JdbcWrapper.closeSession(jdbc);
			}
		}
	  
	  
	  
//	  public void updateintens(BigDecimal codprodorigem,BigDecimal quantidadeorigem, BigDecimal quantidade, BigDecimal nunotaorigem) throws Exception{
//		  
//		  BigDecimal novaquantidade = quantidadeorigem.add(quantidade);
//		  SessionHandle hnd = JapeSession.open();
//	        hnd.setFindersMaxRows(-1);
//	        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
//	        JdbcWrapper jdbc = entity.getJdbcWrapper();
//	        jdbc.openSession();
//	        
//		  try {
//              NativeSql sql = new NativeSql(jdbc);
//              sql.appendSql("UPDATE TGFITE SET QTDNEG = :QUANTIDADE WHERE NUNOTA = :NUNOTA AND CODPROD = :CODPROD");
//              sql.setNamedParameter("NUNOTA", BigDecimal.valueOf(1027430));
//              sql.setNamedParameter("QUANTIDADE", novaquantidade);
//              sql.setNamedParameter("CODPROD", codprodorigem);
//              sql.executeUpdate();
//              
//              updatenunota(nunotaorigem);
//              
//              
//          } catch (Exception e) {
//              e.printStackTrace();
//          } finally {
//              JdbcWrapper.closeSession(jdbc);
//              JapeSession.close(hnd);
//          }
// 	}
	  

		public void adicionaitens(BigDecimal nunota, BigDecimal codprod, BigDecimal quantidade,BigDecimal vlr, BigDecimal nunotaorigem) throws Exception {
				// TODO Auto-generated method stub
				JapeSession.SessionHandle hnd = null;
				
				
				
				try {
					hnd = JapeSession.open();

					JapeWrapper proDAO = JapeFactory.dao("Produto");
					JapeWrapper iteDAO = JapeFactory.dao("ItemNota");

					@SuppressWarnings("unused")
					DynamicVO pro = iteDAO.create()
							.set("CODEMP", BigDecimal.valueOf(1))
							.set("CODPROD", codprod)
							.set("NUNOTA", BigDecimal.valueOf(1027430))
							.set("QTDNEG", quantidade)	
							.set("CODVOL", proDAO.findByPK(codprod).asString("CODVOL"))
							.set("VLRUNIT", vlr)
							.set("VLRTOT", vlr.multiply(quantidade))
							.set("CODLOCALORIG", BigDecimal.valueOf(1030000))
							.set("RESERVA", "S")
							.set("ATUALESTOQUE", BigDecimal.valueOf(1))
							.set("CODVEND",BigDecimal.valueOf(0))
							.set("VLRDESC", BigDecimal.valueOf(0.00))
							.set("PERCDESC", BigDecimal.valueOf(0.00))
							.set("BASEICMS", BigDecimal.valueOf(0.00))
							.set("VLRICMS",  BigDecimal.valueOf(0.00))
							.set("ALIQICMS",  BigDecimal.valueOf(0.00))
							.set("BASEIPI",  BigDecimal.valueOf(0.00))
							.set("VLRIPI",  BigDecimal.valueOf(0.00))
							.set("ALIQIPI",  BigDecimal.valueOf(0.00))
							.set("AD_APONTAMENTO", BigDecimal.valueOf(0.00))
							.set("PENDENTE",  "S")
					        .save();
					
				updatenunota(nunotaorigem);
				
				
				
				}catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);

				} finally {
					JapeSession.close(hnd);
				}}
		
		
		
		public void adicionaitensevox(BigDecimal nunota, BigDecimal codprod, BigDecimal quantidade,BigDecimal vlr, BigDecimal nunotaorigem, BigDecimal nunpedevox) throws Exception {
			// TODO Auto-generated method stub
			JapeSession.SessionHandle hnd = null;
			
			
			
			try {
				hnd = JapeSession.open();

				JapeWrapper proDAO = JapeFactory.dao("Produto");
				JapeWrapper iteDAO = JapeFactory.dao("ItemNota");

				@SuppressWarnings("unused")
				DynamicVO pro = iteDAO.create()
						.set("CODEMP", BigDecimal.valueOf(1))
						.set("CODPROD", codprod)
						.set("NUNOTA", BigDecimal.valueOf(1027430))
						.set("QTDNEG", quantidade)	
						.set("CODVOL", proDAO.findByPK(codprod).asString("CODVOL"))
						.set("VLRUNIT", vlr)
						.set("VLRTOT", vlr.multiply(quantidade))
						.set("CODLOCALORIG", BigDecimal.valueOf(1030000))
						.set("RESERVA", "S")
						.set("ATUALESTOQUE", BigDecimal.valueOf(1))
						.set("CODVEND",BigDecimal.valueOf(0))
						.set("VLRDESC", BigDecimal.valueOf(0.00))
						.set("PERCDESC", BigDecimal.valueOf(0.00))
						.set("BASEICMS", BigDecimal.valueOf(0.00))
						.set("VLRICMS",  BigDecimal.valueOf(0.00))
						.set("ALIQICMS",  BigDecimal.valueOf(0.00))
						.set("BASEIPI",  BigDecimal.valueOf(0.00))
						.set("VLRIPI",  BigDecimal.valueOf(0.00))
						.set("ALIQIPI",  BigDecimal.valueOf(0.00))
						.set("AD_APONTAMENTO", BigDecimal.valueOf(0.00))
						.set("AD_NUNPEDEVOX", nunpedevox)
						.set("PENDENTE",  "S")
				        .save();
				
			updatenunota(nunotaorigem);
			
			
			
			}catch (Exception e) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				e.printStackTrace(pw);

			} finally {
				JapeSession.close(hnd);
			}}

		private void updatenunota(BigDecimal nunotaorigem) throws Exception{
			
			ImpostosHelpper imp = new ImpostosHelpper();
			  SessionHandle hnd = JapeSession.open();
		        hnd.setFindersMaxRows(-1);
		        EntityFacade entity = EntityFacadeFactory.getDWFFacade();
		        JdbcWrapper jdbc = entity.getJdbcWrapper();
		        jdbc.openSession();
		        
			  try {
	          NativeSql sql = new NativeSql(jdbc);
	          sql.appendSql("UPDATE TGFCAB SET AD_OPLANCADA = :LANCADA WHERE NUNOTA = :NUNOTA");
	          sql.setNamedParameter("NUNOTA", nunotaorigem);
	          sql.setNamedParameter("LANCADA", "SIM");
	          sql.executeUpdate();
	          
	          imp.calcularTotalItens(BigDecimal.valueOf(1027430), true);
	          imp.totalizarNota(BigDecimal.valueOf(1027430));
	          
	      } catch (Exception e) {
	          e.printStackTrace();
	      } finally {
	          JdbcWrapper.closeSession(jdbc);
	          JapeSession.close(hnd);
	      }
		}
			  
		
}
 
			    
