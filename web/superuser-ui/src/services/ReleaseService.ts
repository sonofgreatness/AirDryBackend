import { type ReleaseDTO } from "../util/types/common";
import axiosInstance from "../util/axios";

export const ReleaseService = {
   async registerRelease(release: ReleaseDTO) {
        const { data } = await axiosInstance.post("/release/register", release);
        return data;
    },
    async updateRelease(release: ReleaseDTO) {
        const { data } = await axiosInstance.post("/release/update", release);
        return data;
    },
    async loadRelease(id: string)  :Promise<ReleaseDTO>{ 

            const { data } = await axiosInstance.get(`/release/load/${id}`);
        return data;
    
    }, 
    async deleteRelease(id: string) { 

            const { data } = await axiosInstance.delete(`/release/delete/${id}`);
        return data;
    
    },  
    async loadAllReleases() { 
            const { data } = await axiosInstance.get(`/release/load/all`);
        return data;
    
    }, 
    async loadAndroidReleases() { 
            const { data } = await axiosInstance.get(`/release/load/android`);
        return data;
    
    },
    async loadWindowsReleases() { 
            const { data } = await axiosInstance.get(`/release/load/windows`);
        return data;
    
    },


}
