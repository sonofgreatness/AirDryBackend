import { useEffect, useState, useMemo } from "react";
import { WorkService } from "../../services/WorkService";
import { type GetStackDTO} from "../../util/types/common";
import { StackDetailsCard } from "./StackDetailsCard";

export default function StacksLibrary() {
  const [stacks, setStacks] = useState<GetStackDTO[]>([]);
  const [loadingStacks, setLoadingStacks] = useState(true);
  // 1. New state to hold the user's search query
  const [searchQuery, setSearchQuery] = useState(''); 

  useEffect(() => {
    (async () => {
      try {
        const result = await WorkService.getAllStacks();
        setStacks(result ?? []); 
      } catch (err) {
        console.error(err);
        setStacks([]);
      }
      setLoadingStacks(false);
    })();
  }, []);

  // 2. Memoized list filtering logic
  const filteredStacks = useMemo(() => {
    if (!searchQuery) {
        return stacks; // Return all stacks if the query is empty
    }

    const lowerCaseQuery = searchQuery.toLowerCase();

    return stacks.filter(stack => {
        // Check for case-insensitive match across relevant fields
        const matchDisplayName = stack.displayName.toLowerCase().includes(lowerCaseQuery);
        const matchStackCodeName = stack.stackCode.displayName.toLowerCase().includes(lowerCaseQuery);
        const matchStackCodeValue = stack.stackCode.displayCode.toLowerCase().includes(lowerCaseQuery);

        return matchDisplayName || matchStackCodeName || matchStackCodeValue;
    });
  }, [stacks, searchQuery]); // Re-run filtering only when stacks or searchQuery changes

  return (
    <div className="page-padd">
      <div className="d-flex justify-content-between align-items-center mb-4">
        <div><h2 className="intro-title display-6 fw-bold">Stacks Library</h2></div>
        {/* Export Dropdown/FAB logic goes here */}
      </div>

      {/* 3. Search Input Field (Bootstrap) */}
      <div className="mb-4">
        <input
          type="text"
          className="form-control form-control-lg"
          placeholder="Search stacks by name or code..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
      </div>
      
      {loadingStacks ? (
        <div className="d-flex justify-content-center align-items-center" style={{ height: '10rem' }}>
          <div className="spinner-border text-primary" role="status">
             <span className="visually-hidden">Loading...</span>
          </div>
          <p className="ms-2 text-muted">Loading work data...</p>
        </div>
      ) : stacks.length === 0 ? (
        <div className="text-center text-muted mt-5">No stacks found.</div>
      ) : filteredStacks.length === 0 ? (
        // Display message if the search yields no results
        <div className="text-center text-muted mt-5">
            No stacks match your search for **"{searchQuery}"**.
        </div>
      ) : (
        // Use the filteredStacks list for rendering the grid
        <div className="row row-cols-1 row-cols-md-2 g-4"> 
          {filteredStacks.map((stack) => (
            <div className="col" key={stack.id}> 
                <StackDetailsCard 
                    stack={stack} 
                    onClick={() => { /* Add navigation logic */ }} 
                />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}