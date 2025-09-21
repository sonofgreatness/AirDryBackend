import flag from '../../assets/footer/flag.png'
import coa from '../../assets/footer/coa.png'
import phone from '../../assets/icons/phone-solid.svg'
import user from '../../assets/icons/user-solid1.svg'
/*import util from '../../../utils/util'
*/
const Footer = () => {
  return (

    <div style={{marginTop:"4%"}} >
    
    <div className='footer-height shadow-sm  container-sm gray ' style={{height:"8%"}}>
  
  <div >
  <div className=' px-5 '>


  <div className='px-3 d-flex justify-content-between mb-5'>

          <ul className='d-flex list-unstyled mx-5 pt-3'>
              <li className='mx-3'><img src={flag} alt="flag" style={{height:"25%"}} className=' rounded-lg'/></li>
              <li className='mx-3'><img src={coa} alt="coa" style={{height:"25%"}} /></li>

          </ul>  
      <p className='blue'> <img src={phone} alt="phone Icon" height={12} />  Help Line:76911464/79487461 <br /><img src={user} alt="phone Icon" height={12} />  Logged in as: Simphiwe Magagula</p>

  </div>
</div>
  </div>


</div>
    </div>



  )
}

export default Footer