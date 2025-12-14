import { useEffect, useState } from "react";
import { WorkService } from "../../services/WorkService";
import { type WeeklyApprovalDTO, type WorkDaySummaryDTO } from "../../util/types/common";
import dayjs from "dayjs";
import { DEFAULT_PAGE_SIZE } from "../../util/constants"; 
import { Link } from "react-router-dom";

export default function UserStats() {
  const [weekly, setWeekly] = useState<WeeklyApprovalDTO | null>(null);
  const [loadingWeekly, setLoadingWeekly] = useState(true);

  const [startDate, setStartDate] = useState<string>("");
  const [endDate, setEndDate] = useState<string>("");

  const [workData, setWorkData] = useState<WorkDaySummaryDTO[]>([]);
  const [loadingWork, setLoadingWork] = useState(false);

  const [page, setPage] = useState(1);
  const [pageSize] = useState(DEFAULT_PAGE_SIZE);
  const [totalPages, setTotalPages] = useState(1);

  const [expanded, setExpanded] = useState<Record<number, boolean>>({});

  // ------------------------------------------
  // Load Latest Weekly Approval + PREPOPULATE DATES
  // ------------------------------------------
  useEffect(() => {
    (async () => {
      try {
        const result = await WorkService.getLatestApprovedWeek();
        setWeekly(result ?? null);

        if (result) {
          const approvedEnd = dayjs(result.weekEndDate);

          // FORM PREPOPULATION RULES
          const defaultStart = dayjs().subtract(6, "month").format("YYYY-MM-DD");
          const defaultEnd = approvedEnd.subtract(1, "day").format("YYYY-MM-DD");

          setStartDate(defaultStart);
            setEndDate(defaultEnd);
        loadWork()
        }

      } catch (err) {
        console.error(err);
      }
      setLoadingWeekly(false);
    })();
  }, []);

  // ------------------------------------------
  // Fetch Work Data (Paginated)
  // ------------------------------------------
  const loadWork = async (pageNum: number = page) => {
    setLoadingWork(true);
    try {
      const result = await WorkService.getWork(
        pageNum,
        pageSize,
        startDate || undefined,
        endDate || undefined
      );

      setWorkData(result.items);
      setTotalPages(result.totalPages);
    } catch (err) {
      console.error(err);
    }
    setLoadingWork(false);
  };

  // ------------------------------------------
  // Apply Filters WITH VALIDATION
  // ------------------------------------------
  const applyFilters = () => {
    if (!weekly) {
      alert("No approved week available. Records cannot be viewed yet.");
      return;
    }

    const approvedEnd = dayjs(weekly.weekEndDate);

    // VALIDATION RULES
    if (
      (startDate && dayjs(startDate).isAfter(approvedEnd)) ||
      (endDate && dayjs(endDate).isAfter(approvedEnd))
    ) {
      alert(
        "You cannot view records beyond the approved week. " +
        "Please wait until the week is approved."
      );
      return;
    }

    setPage(1);
    loadWork(1);
  };

  const toggleExpand = (index: number) => {
    setExpanded((prev) => ({
      ...prev,
      [index]: !prev[index],
    }));
  };

  return (
    <div className="page-padd">
      <div className="page-head uc d-flex">
        <div><h2 className="intro-title mb-3">Your Stats</h2>
        <Link to="/stacks_library" className="portal-login-link">
                    View Stacks Library →
        </Link>
        </div>
      </div>

      {/* =========================== */}
      {/*  WEEKLY SUMMARY CARD        */}
      {/* =========================== */}
      <div className="sticky-top bg-white p-3 mb-3 shadow-sm rounded">
        {loadingWeekly ? (
          <div>Loading week summary...</div>
        ) : weekly ? (
          <div>
            <h4 className="mb-1">Latest Approved Week</h4>
            <div className="text-muted">
              {dayjs(weekly.weekStartDate).format("DD MMM YYYY")} -{" "}
              {dayjs(weekly.weekEndDate).format("DD MMM YYYY")}
            </div>
            <div>Status: <strong>{weekly.status}</strong></div>
            {weekly.approvedByFullName && (
              <div className="mt-1">
                Approved By: <strong>{weekly.approvedByFullName}</strong>
              </div>
            )}
          </div>
        ) : (
          <div>No approved week found.</div>
        )}
      </div>

      {/* =========================== */}
      {/*  FILTER FORM                */}
      {/* =========================== */}
      <form
        className="filter-form mb-4"
        onSubmit={(e) => {
          e.preventDefault();
          applyFilters();
        }}
      >
        <input
          type="date"
          value={startDate}
          max={weekly?.weekEndDate}
          onChange={(e) => setStartDate(e.target.value)}
          placeholder="Start Date"
        />

        <input
          type="date"
          value={endDate}
          max={weekly?.weekEndDate}
          onChange={(e) => setEndDate(e.target.value)}
          placeholder="End Date"
        />

        <button type="submit" className="gradient-btn-small">
          Apply
        </button>
      </form>

      {/* =========================== */}
      {/*  WORK DATA                  */}
      {/* =========================== */}
      {loadingWork ? (
        <div className="loader">
          <div className="spinner"></div>
          <p>Loading work data...</p>
        </div>
      ) : workData.length === 0 ? (
        <div className="text-center text-muted">No work found.</div>
      ) : (
        <>
          {workData.map((day, idx) => (
            <div
              key={idx}
              className="p-3 mb-3 bg-white shadow-sm rounded border"
            >
              <div
                className="d-flex justify-content-between align-items-center cpointer"
                onClick={() => toggleExpand(idx)}
              >
                <h5 className="mb-0">
                  {day.dayLabel} — {floorTo3(day.totalVolume)} cubes | {day.totalStacks} Stacks | {day.totalUsers} Users
                </h5>
                <div>{expanded[idx] ? "▲" : "▼"}</div>
              </div>

              {expanded[idx] && (
                <div className="mt-3">
                  {day.workList.map((w, jdx) => (
                    <div
                      key={jdx}
                      className="border rounded p-2 mb-2"
                      style={{ background: "#fdfdfd" }}
                    >
                      <strong>{w.workDate}</strong>
                      <div className="text-muted small">
                        Total Cubes: {floorTo3(w.totalVolume)} | Stacks: {w.totalStacks}
                      </div>
                      <div className="mt-2">
                        <strong>Stacks:</strong>
                        {w.stacks.map((s) => (
                          <div key={s.id} className="ms-3">
                            {s.displayName} {s.stackCode} {s.stackCodeDisplayName} — {floorTo3(s.volume)} cubes
                          </div>
                        ))}
                      </div>
                      <div className="mt-2">
                        <strong>Owners:</strong>
                        {w.users.map((u) => (
                          <div key={u.id} className="ms-3">
                            {u.name} {u.surname} -- {floorTo3(w.totalVolume / w.users.length)}
                          </div>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ))}

          {/* =========================== */}
          {/*  PAGINATION                 */}
          {/* =========================== */}
          <div className="pagination mt-4">
            <button
              disabled={page === 1}
              onClick={() => {
                setPage(page - 1);
                loadWork(page - 1);
              }}
            >
              Previous
            </button>

            <span>
              Page {page} / {totalPages}
            </span>

            <button
              disabled={page === totalPages}
              onClick={() => {
                setPage(page + 1);
                loadWork(page + 1);
              }}
            >
              Next
            </button>
          </div>
        </>
      )}
    </div>
  );
}

function floorTo3(value: number): number {
  return Math.floor(value * 1000) / 1000;
}
