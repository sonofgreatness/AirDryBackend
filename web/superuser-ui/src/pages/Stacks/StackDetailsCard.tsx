import React from 'react';
import { type GetStackDTO } from "../../util/types/common";

// Helper for formatting volume/dimensions
const floorTo3 = (value: number): number => {
    return Math.floor(value * 1000) / 1000;
};

// Helper function to format volume to three decimal places
const formatVolume = (value: number | null | undefined): string => {
    if (value === null || value === undefined) return "0.000";
    return floorTo3(value).toFixed(3);
};

// Row for key metrics (like Pieces and Cubes)
const KeyMetricRow: React.FC<{ label: string; value: string }> = ({ label, value }) => (
    <div className="d-flex justify-content-between w-100">
        <span className="fw-semibold small">{label}</span>
        <span className="text-secondary small">{value}</span>
    </div>
);

// Row for dimensions
const DimensionRow: React.FC<{ height: number; length: number; thickness: number }> = ({ height, length, thickness }) => (
    <div className="d-flex flex-column w-100 mt-2">
        <span className="fw-bold text-muted small mb-1">Dims (H x L x T)</span>
        <span className="text-secondary small">
            {floorTo3(height)} x {floorTo3(length)} x {floorTo3(thickness)}
        </span>
    </div>
);


export const StackDetailsCard: React.FC<{ stack: GetStackDTO, onClick: () => void }> = ({ stack, onClick }) => {
    return (
        <div
            // Bootstrap card classes: card, shadow, rounded corners, padding
            className="card shadow-sm rounded-3 p-3 cursor-pointer h-100"
            onClick={onClick}
            style={{ transition: 'box-shadow 0.2s', backgroundColor: '#f8f9fa' }} // Simulate surfaceVariant color
        >
            <div className="d-flex flex-column">
                {/* Primary Identification (Title) */}
                <h3 className="h6 fw-semibold text-primary text-truncate mb-1">
                    {stack.displayName}
                </h3>

                {/* Secondary Identification (Subtitle) */}
                <p className="text-muted small mb-3">
                    {stack.stackCode.displayName} ({stack.stackCode.displayCode})
                </p>

                {/* Divider */}
                <hr className="my-2 text-black-50" style={{ opacity: 0.1 }} />

                {/* Key Metrics */}
                <KeyMetricRow label="Pieces  " value={stack.numberOfPieces.toString()} />
                <KeyMetricRow label="Cubes (m³)  " value={formatVolume(stack.volume)} />

                {/* Dimensions */}
                <DimensionRow
                    height={stack.height}
                    length={stack.length}
                    thickness={stack.thickness}
                />
            </div>
        </div>
    );
};