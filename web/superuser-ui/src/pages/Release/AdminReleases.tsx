import { ReleaseService } from "../../services/ReleaseService"
import { type ReleaseDTO } from "../../util/types/common"
import { useNavigate } from "react-router-dom";
import  { useEffect, useState } from "react";


export default function AdminReleases() { 
const navigate = useNavigate();
    
const [allReleases, setAllReleases] = useState<ReleaseDTO[]>([]); 

async function loadReleases() {
    try {
        const res = await ReleaseService.loadAllReleases(); 
        setAllReleases(res); 
     }
    catch (error) { 
     console.log("Failed to load releases", JSON.stringify(error))
    }
 }
 async function deleteRelease(id:string) {
     try {
         await ReleaseService.deleteRelease(id); 
        navigate('/releases/all')
     }
    catch (error) { 
     console.log("Failed to load releases", JSON.stringify(error))
    }
 }   

useEffect(() => { 
        loadReleases()
})
    
    return (
        <div className="user-page">
            <h1>
              All Releases   
            </h1>
            <br></br>
                   <div className="my_wide_button"
        style={{ width: '100%' }}
        >
          <button type="submit"
            style={{
              width: '20%'
             }}
            className="ticket-btn"
          onClick={() => navigate('/releases/create')}>
            CREATE 
          </button> 
      </div>
          <table className="user-table">
            <thead>
              <tr>
                <th>Platform </th>
                <th>Link</th>
                <th>Upload date</th>
              </tr>
            </thead>
            <tbody> 
            {
            allReleases.length === 0 ? (
                <tr>
                  <td colSpan={3} className="no-users">
                    No releases found
                  </td>
                </tr>
                    ) : (
                            allReleases.map((release) => (

                                <tr key={release.id}>
                                    <td>
                                        { release.platform}
                                    </td>
                                    <td>
                                        { release.link}
                                    </td>
                                    <td>
                                        { release.dateOfRelease}
                                    </td>
                                    <td>
           <button
                        type="button"
                        onClick={() => navigate(`/releases/${release.id}/edit`)}
                        className="gradient-btn-small"
                      >
                        Edit
                      </button>
                    </td>
                    <td>
                      <button
                         type="button"
                        onClick={() => deleteRelease(`${release.id}`)}
                        className="ticket-btn-delete">Delete
                      </button>
                    </td>
                    </tr>))
                        )
                    
                }
            </tbody>
            </table>
            </div>
    
    ) 
}