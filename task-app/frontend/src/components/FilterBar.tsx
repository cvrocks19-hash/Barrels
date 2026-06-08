import type { TaskFilters, TaskStatus } from '../types/task';

interface Props {
  filters: TaskFilters;
  onChange: (f: TaskFilters) => void;
}

const STATUS_TABS: { value: TaskStatus | ''; label: string }[] = [
  { value: '',            label: 'All'         },
  { value: 'todo',        label: 'To Do'       },
  { value: 'in-progress', label: 'In Progress' },
  { value: 'done',        label: 'Done'        },
];

export default function FilterBar({ filters, onChange }: Props) {
  return (
    <div className="mb-4 space-y-2">
      <input
        type="search"
        placeholder="Search tasks…"
        value={filters.q ?? ''}
        onChange={(e) => onChange({ ...filters, q: e.target.value || undefined })}
        className="w-full border border-gray-200 rounded-xl px-3 py-2 text-sm bg-white
                   focus:outline-none focus:ring-2 focus:ring-blue-500"
      />
      <div className="flex gap-2 overflow-x-auto pb-1 scrollbar-none">
        {STATUS_TABS.map((tab) => {
          const active = (filters.status ?? '') === tab.value;
          return (
            <button
              key={tab.value}
              onClick={() => onChange({ ...filters, status: tab.value || undefined })}
              className={`whitespace-nowrap px-3 py-1 rounded-full text-xs font-medium transition-colors
                ${ active
                  ? 'bg-blue-600 text-white'
                  : 'bg-white text-gray-600 border border-gray-200 hover:bg-gray-50'
                }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );
}
