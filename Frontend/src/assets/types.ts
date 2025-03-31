export interface License {
    id: number;
    name: string;
}

export interface Zone {
    id: number;
    name: string;
    capacity: number;
    tasks: Task[];
    workers: Worker[];
}

export interface Task {
    id: number;
    name: string;
    description: string;
    minWorkers: number;
    maxWorkers: number;
    requiredLicense: License[];
    zoneId: number;
    maxTime: number;
    minTime: number;
}

export interface ActiveTask {
    id: number;
    dueDate: string;
    startTime: string;
    endTime: string;
    eta: string;
    date: string;
    task: Task[];
    workers: Worker[];
    strictStart: boolean;
}

export interface Worker {
    id: number;
    name: string;
    zone: number;
    efficiency: number;
    available: boolean;
    breakStartTime: string;
    dead: boolean;
    licenses: License[];
    workerType: string;
}
